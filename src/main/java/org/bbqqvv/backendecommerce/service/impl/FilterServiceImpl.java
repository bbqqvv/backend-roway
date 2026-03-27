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

    private static final String FILTER_CACHE_KEY = "catalog:filter:options:v2";

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
        List<Map<String, String>> colors = productRepository.findDistinctColorsWithHex()
                .stream()
                .map(arr -> {
                    Map<String, String> m = new java.util.HashMap<>();
                    m.put("name", (String) arr[0]);
                    m.put("hexCode", (String) arr[1]);
                    return m;
                })
                .toList();
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

        Map<String, Object> filterOptions = new java.util.HashMap<>();
        filterOptions.put("colors", colors != null ? colors : List.of());
        filterOptions.put("sizes", sizes != null ? sizes : List.of());
        filterOptions.put("tags", tags != null ? tags : List.of());
        filterOptions.put("minPrice", minPrice != null ? minPrice : BigDecimal.ZERO);
        filterOptions.put("maxPrice", maxPrice != null ? maxPrice : BigDecimal.ZERO);
        filterOptions.put("categories", categories != null ? categories : List.of());


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

        // Filter by category (slug or id)
        String categoriesParam = allParams.containsKey("categories") ? allParams.get("categories") : allParams.get("category");
        if (categoriesParam != null && !categoriesParam.isBlank()) {
            String[] categoryVals = categoriesParam.split(",");
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Product, Category> categoryJoin = root.join("category");
                
                try {
                    // Try parsing as numeric IDs (from frontend Redux state)
                    List<Long> ids = java.util.Arrays.stream(categoryVals)
                                            .map(Long::valueOf)
                                            .toList();
                    return categoryJoin.get("id").in(ids);
                } catch (NumberFormatException e) {
                    // Fallback to Slug matching
                    return categoryJoin.get("slug").in((Object[]) categoryVals);
                }
            });
        }

        // Filter by tag
        String tagsParam = allParams.containsKey("tags") ? allParams.get("tags") : allParams.get("tag");
        if (tagsParam != null && !tagsParam.isBlank()) {
            String[] tagNames = tagsParam.split(",");
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Product, Tag> tagJoin = root.join("tags", jakarta.persistence.criteria.JoinType.LEFT);
                return tagJoin.get("name").in((Object[]) tagNames);
            });
        }

        // Filter by price range
        if (allParams.containsKey("minPrice") || allParams.containsKey("maxPrice")) {
            String minStr = allParams.get("minPrice");
            String maxStr = allParams.get("maxPrice");
            
            BigDecimal minPrice = (minStr != null && !minStr.isBlank()) 
                    ? new BigDecimal(minStr) : BigDecimal.ZERO;
            BigDecimal maxPrice = (maxStr != null && !maxStr.isBlank()) 
                    ? new BigDecimal(maxStr) : new BigDecimal("9999999999");

            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Product, ProductVariant> variantJoin = root.join("variants");
                Join<ProductVariant, SizeProductVariant> sizeVariantJoin = variantJoin.join("productVariantSizes");
                Join<SizeProductVariant, SizeProduct> sizeJoin = sizeVariantJoin.join("sizeProduct");
                return cb.between(sizeJoin.get("price"), minPrice, maxPrice);
            });
        }

        // Filter by color
        String colorsParam = allParams.containsKey("colors") ? allParams.get("colors") : allParams.get("color");
        if (colorsParam != null && !colorsParam.isBlank()) {
            String[] colors = colorsParam.split(",");
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Product, ProductVariant> variantJoin = root.join("variants");
                return variantJoin.get("color").in((Object[]) colors);
            });
        }

        // Filter by size
        String sizesParam = allParams.containsKey("sizes") ? allParams.get("sizes") : allParams.get("size");
        if (sizesParam != null && !sizesParam.isBlank()) {
            String[] sizeNames = sizesParam.split(",");
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Product, ProductVariant> variantJoin = root.join("variants");
                Join<ProductVariant, SizeProductVariant> sizeVariantJoin = variantJoin.join("productVariantSizes");
                Join<SizeProductVariant, SizeProduct> sizeJoin = sizeVariantJoin.join("sizeProduct");
                return sizeJoin.get("sizeName").in((Object[]) sizeNames);
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
