package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.ProductMapper;
import org.bbqqvv.backendecommerce.repository.*;
import org.bbqqvv.backendecommerce.service.ProductService;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.bbqqvv.backendecommerce.util.SlugUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    SizeProductRepository sizeProductRepository;
    ProductMapper productMapper;
    CloudinaryService cloudinaryService;
    TagRepository tagRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductMapper productMapper,
                              CloudinaryService cloudinaryService,
                              SizeProductRepository sizeProductRepository,
                              TagRepository tagRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.cloudinaryService = cloudinaryService;
        this.sizeProductRepository = sizeProductRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        if (productRepository.existsByProductCode(productRequest.getProductCode())) {
            throw new AppException(ProductErrorCode.DUPLICATE_PRODUCT_CODE);
        }

        Category category = getCategoryById(productRequest.getCategoryId());
        Product product = createOrUpdateProductEntity(productRequest, category);

        product.setSlug(generateUniqueSlug(productRequest.getName()));
        Product savedProduct = productRepository.save(product);
        return toFullProductResponse(savedProduct);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::toFullProductResponse)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return toPageResponse(productPage);
    }

    @Override
    public PageResponse<ProductResponse> findProductByCategorySlug(String slug, Pageable pageable) {
        Category category = categoryRepository.findBySlug(slug);
        if (category == null) throw new AppException(ProductErrorCode.CATEGORY_NOT_FOUND);
        Page<Product> productPage = productRepository.findProductByCategory(category, pageable);
        return toPageResponse(productPage);
    }

    @Override
    public ProductResponse getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(this::toFullProductResponse)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

        Category category = getCategoryById(productRequest.getCategoryId());
        Product product = createOrUpdateProductEntity(productRequest, category);
        product.setId(id);

        if (!existingProduct.getName().equals(productRequest.getName())) {
            product.setSlug(generateUniqueSlug(productRequest.getName()));
        } else {
            product.setSlug(existingProduct.getSlug());
        }

        Product savedProduct = productRepository.save(product);
        return toFullProductResponse(savedProduct);
    }

    @Override
    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new AppException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
        return true;
    }

    @Override
    public PageResponse<ProductResponse> searchProductsByName(String name, Pageable pageable) {
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return toPageResponse(products);
    }

    @Override
    public PageResponse<ProductResponse> getFeaturedProducts(Pageable pageable) {
        Page<Product> featured = productRepository.findByFeaturedTrue(pageable);
        return toPageResponse(featured);
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));
    }

    private Product createOrUpdateProductEntity(ProductRequest req, Category category) {
        Product product = productMapper.toProduct(req);
        product.setCategory(category);

        handleImageUrls(product, req);

        if (req.getTags() != null) {
            Set<Tag> tags = req.getTags().stream()
                    .map(name -> tagRepository.findByName(name)
                            .orElseGet(() -> tagRepository.save(new Tag(name))))
                    .collect(Collectors.toSet());
            product.setTags(tags);
        }

        if (req.getVariants() != null) {
            List<ProductVariant> variants = req.getVariants().stream().map(variantRequest -> {
                ProductVariant variant = new ProductVariant();
                variant.setColor(variantRequest.getColor());
                variant.setProduct(product);

                Optional.ofNullable(variantRequest.getImageUrl())
                        .map(cloudinaryService::uploadImage)
                        .ifPresent(variant::setImageUrl);

                List<SizeProductVariant> sizeVariants = Optional.ofNullable(variantRequest.getSizes())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(sizeRequest -> {
                            SizeProduct size = sizeProductRepository.findBySizeName(sizeRequest.getSizeName())
                                    .orElseGet(() -> {
                                        SizeProduct s = new SizeProduct();
                                        s.setSizeName(sizeRequest.getSizeName());
                                        s.setPrice(sizeRequest.getPrice());
                                        s.setPriceAfterDiscount(calculatePriceAfterDiscount(
                                                sizeRequest.getPrice(), req.getSalePercentage()));
                                        return sizeProductRepository.save(s);
                                    });

                            SizeProductVariant spv = new SizeProductVariant();
                            spv.setProductVariant(variant);
                            spv.setSizeProduct(size);
                            spv.setStock(sizeRequest.getStock());
                            return spv;
                        }).collect(Collectors.toList());

                variant.setProductVariantSizes(sizeVariants);
                return variant;
            }).collect(Collectors.toList());

            product.setVariants(variants);
        }

        return product;
    }

    private void handleImageUrls(Product product, ProductRequest req) {
        try {
            Optional.ofNullable(req.getMainImageUrl())
                    .map(cloudinaryService::uploadImage)
                    .ifPresent(url -> product.setMainImage(ProductMainImage.builder()
                            .imageUrl(url).product(product).build()));

            Optional.ofNullable(req.getSecondaryImageUrls())
                    .filter(list -> !list.isEmpty())
                    .map(cloudinaryService::uploadImages)
                    .ifPresent(urls -> product.setSecondaryImages(urls.stream()
                            .map(url -> ProductSecondaryImage.builder().imageUrl(url).product(product).build())
                            .collect(Collectors.toList())));

            Optional.ofNullable(req.getDescriptionImageUrls())
                    .filter(list -> !list.isEmpty())
                    .map(cloudinaryService::uploadImages)
                    .ifPresent(urls -> product.setDescriptionImages(urls.stream()
                            .map(url -> ProductDescriptionImage.builder().imageUrl(url).product(product).build())
                            .collect(Collectors.toList())));
        } catch (Exception e) {
            throw new AppException(InfrastructureAddressErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private String generateUniqueSlug(String name) {
        String base = SlugUtils.toSlug(name);
        List<String> existingSlugs = productRepository.findSlugsByPattern(base + "%");
        
        if (!existingSlugs.contains(base)) {
            return base;
        }

        int count = 1;
        String slug = base + "-" + count;
        while (existingSlugs.contains(slug)) {
            count++;
            slug = base + "-" + count;
        }
        return slug;
    }

    private BigDecimal calculatePriceAfterDiscount(BigDecimal price, int percent) {
        if (price != null && percent > 0) {
            BigDecimal discount = price.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));
            return price.subtract(discount);
        }
        return price;
    }

    private PageResponse<ProductResponse> toPageResponse(Page<Product> page) {
        List<Long> productIds = page.getContent().stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<Long, Integer> reviewCounts = productRepository.countReviewsByProductIds(productIds).stream()
                .collect(Collectors.toMap(
                        obj -> (Long) obj[0],
                        obj -> ((Long) obj[1]).intValue()
                ));

        List<ProductResponse> items = page.getContent().stream()
                .map(product -> {
                    ProductResponse res = productMapper.toProductResponse(product);
                    res.setReviewCount(reviewCounts.getOrDefault(product.getId(), 0));
                    return res;
                })
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .items(items)
                .build();
    }

    private ProductResponse toFullProductResponse(Product product) {
        ProductResponse res = productMapper.toProductResponse(product);
        long reviewCount = productRepository.countReviewsByProductId(product.getId());
        res.setReviewCount((int) reviewCount);
        return res;
    }
}

