package org.bbqqvv.backendecommerce.service.impl;

import jakarta.persistence.criteria.Join;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.response.CategoryResponseForFilter;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.mapper.ProductMapper;
import org.bbqqvv.backendecommerce.repository.CategoryRepository;
import org.bbqqvv.backendecommerce.repository.OrderRepository;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
import org.bbqqvv.backendecommerce.service.FilterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.bbqqvv.backendecommerce.util.PagingUtil.toPageResponse;

@Service
public class FilterServiceImpl implements FilterService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final ProductMapper productMapper;

    private static final String FILTER_CACHE_KEY = "catalog:filter:options";

    public FilterServiceImpl(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate,
                             com.fasterxml.jackson.databind.ObjectMapper objectMapper,
                             ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.productMapper = productMapper;
    }

    @Override
    @lombok.SneakyThrows
    public Map<String, Object> getFilterOptions() {
        // 1. Thử lấy từ Redis
        String cachedJson = redisTemplate.opsForValue().get(FILTER_CACHE_KEY);
        if (cachedJson != null && !cachedJson.isBlank()) {
            return objectMapper.readValue(cachedJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        }

        // 2. Nếu không có ở Redis, Query từ DB
        List<String> colors = productRepository.findDistinctColors();
        List<String> sizes = productRepository.findDistinctSizes();
        List<String> tags = productRepository.findDistinctTags();
        BigDecimal minPrice = productRepository.findMinPrice();
        BigDecimal maxPrice = productRepository.findMaxPrice();
        List<CategoryResponseForFilter> categories = categoryRepository.findAll()
                .stream()
                .map(c -> CategoryResponseForFilter.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .slug(c.getSlug())
                        .build())
                .toList();

        Map<String, Object> filterOptions = Map.of(
                "colors", colors,
                "sizes", sizes,
                "tags", tags,
                "minPrice", minPrice,
                "maxPrice", maxPrice,
                "categories", categories
        );

        // 3. Lưu vào Redis với TTL 30 phút
        redisTemplate.opsForValue().set(FILTER_CACHE_KEY, 
                objectMapper.writeValueAsString(filterOptions), 
                java.time.Duration.ofMinutes(30));

        return filterOptions;
    }

    @Override
    public PageResponse<ProductResponse> filterProducts(Map<String, String> allParams, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        // 🔍 Filter by keyword (Search)
        if (allParams.containsKey("keyword") && !allParams.get("keyword").isBlank()) {
            String keyword = allParams.get("keyword").toLowerCase();
            spec = spec.and((root, query, cb) -> {
                Join<Product, Tag> tagJoin = root.join("tags", jakarta.persistence.criteria.JoinType.LEFT);
                return cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + keyword + "%"),
                    cb.like(cb.lower(root.get("summary")), "%" + keyword + "%"),
                    cb.like(cb.lower(tagJoin.get("name")), "%" + keyword + "%")
                );
            });
        }

        // Filter by category (slug)
        if (allParams.containsKey("category")) {
            String categorySlug = allParams.get("category");
            spec = spec.and((root, query, cb) -> {
                Join<Product, Category> categoryJoin = root.join("category");
                return cb.equal(categoryJoin.get("slug"), categorySlug);
            });
        }

        // Filter by tag
        if (allParams.containsKey("tag")) {
            String tagName = allParams.get("tag");
            spec = spec.and((root, query, cb) -> {
                Join<Product, Tag> tagJoin = root.join("tags");
                return cb.equal(tagJoin.get("name"), tagName);
            });
        }

        // Filter by price range
        if (allParams.containsKey("minPrice") || allParams.containsKey("maxPrice")) {
            BigDecimal minPrice = allParams.getOrDefault("minPrice", null) != null 
                    ? new BigDecimal(allParams.get("minPrice")) 
                    : BigDecimal.ZERO;
            BigDecimal maxPrice = allParams.getOrDefault("maxPrice", null) != null 
                    ? new BigDecimal(allParams.get("maxPrice")) 
                    : new BigDecimal("9999999999");

            spec = spec.and((root, query, cb) -> {
                Join<Product, ProductVariant> variantJoin = root.join("variants");
                Join<ProductVariant, SizeProductVariant> sizeVariantJoin = variantJoin.join("productVariantSizes");
                Join<SizeProductVariant, SizeProduct> sizeJoin = sizeVariantJoin.join("sizeProduct");
                return cb.between(sizeJoin.get("price"), minPrice, maxPrice);
            });
        }

        // Filter by color
        if (allParams.containsKey("color")) {
            String color = allParams.get("color");
            spec = spec.and((root, query, cb) -> {
                Join<Product, ProductVariant> variantJoin = root.join("variants");
                return cb.equal(variantJoin.get("color"), color);
            });
        }

        // Filter by size
        if (allParams.containsKey("size")) {
            String sizeName = allParams.get("size");
            spec = spec.and((root, query, cb) -> {
                Join<Product, ProductVariant> variantJoin = root.join("variants");
                Join<ProductVariant, SizeProductVariant> sizeVariantJoin = variantJoin.join("productVariantSizes");
                Join<SizeProductVariant, SizeProduct> sizeJoin = sizeVariantJoin.join("sizeProduct");
                return cb.equal(sizeJoin.get("sizeName"), sizeName);
            });
        }

        // Filter by onSale
        if (allParams.containsKey("onSale") && Boolean.parseBoolean(allParams.get("onSale"))) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThan(root.get("salePercentage"), 0));
        }

        // 🟢 Handle Sorting (Tùy chỉnh nếu frontend truyền sortBy=price-asc...)
        if (allParams.containsKey("sortBy")) {
            String sortBy = allParams.get("sortBy");
            // Ghi đè sort của pageable nếu cần, hoặc đơn giản là xử lý trong query
            // Ở đây Pageable thường đã được Spring xử lý nếu truyền ?sort=...
            // Nhưng nếu frontend dùng chuẩn riêng:
            /*
            switch (sortBy) {
                case "price-asc" -> ...
                case "price-desc" -> ...
                case "newest" -> ...
            }
            */
        }

        // Query and map
        // Sử dụng EntityGraph để fetch các quan hệ cần thiết tránh N+1
        Page<Product> filteredProducts = productRepository.findAll(spec, pageable);
        return toPageResponse(filteredProducts, productMapper::toProductResponse);
    }
}
