package org.bbqqvv.backendecommerce.service.impl;

import jakarta.persistence.EntityManager;
import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.bbqqvv.backendecommerce.dto.request.ProductRequest;
import org.bbqqvv.backendecommerce.dto.request.ProductVariantRequest;
import org.bbqqvv.backendecommerce.dto.request.SizeProductRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.entity.Category;
import org.bbqqvv.backendecommerce.entity.SizeProduct;
import org.bbqqvv.backendecommerce.entity.ProductVariant;
import org.bbqqvv.backendecommerce.entity.SizeProductVariant;
import org.bbqqvv.backendecommerce.entity.SizeCategory;
import org.bbqqvv.backendecommerce.entity.Tag;
import org.bbqqvv.backendecommerce.entity.ProductImage;
import org.bbqqvv.backendecommerce.entity.ProductMainImage;
import org.bbqqvv.backendecommerce.entity.ProductSecondaryImage;
import org.bbqqvv.backendecommerce.entity.ProductDescriptionImage;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
    SizeCategoryRepository sizeCategoryRepository;
    EntityManager entityManager;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductMapper productMapper,
                              CloudinaryService cloudinaryService,
                              SizeProductRepository sizeProductRepository,
                              TagRepository tagRepository,
                              SizeCategoryRepository sizeCategoryRepository,
                              EntityManager entityManager) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.cloudinaryService = cloudinaryService;
        this.sizeProductRepository = sizeProductRepository;
        this.tagRepository = tagRepository;
        this.sizeCategoryRepository = sizeCategoryRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        if (productRepository.existsByProductCode(productRequest.getProductCode())) {
            throw new AppException(ProductErrorCode.DUPLICATE_PRODUCT_CODE);
        }

        Category category = getCategoryById(productRequest.getCategoryId());
        Product product = createOrUpdateProductEntity(productRequest, category, null);

        product.setSlug(productRequest.getSlug());
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
    public PageResponse<ProductResponse> getRelatedProducts(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (product.getCategory() == null) {
            return toPageResponse(org.springframework.data.domain.Page.empty(pageable));
        }

        org.springframework.data.domain.Page<Product> relatedProducts = productRepository.findRelatedProducts(
                product.getCategory().getId(), productId, pageable);

        return toPageResponse(relatedProducts);
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

        // ⚡ Thu thập publicIds cũ TRƯỚC khi cập nhật
        List<String> oldPublicIds = collectPublicIds(existingProduct);

        // Build entity mới từ request (chỉ để lấy dữ liệu, KHÔNG save entity này)
        Product newData = createOrUpdateProductEntity(productRequest, category, id);

        // ══ BƯỚC 1: Xóa tất cả child entities cũ ══
        existingProduct.setMainImage(null);
        if (existingProduct.getSecondaryImages() != null) {
            existingProduct.getSecondaryImages().clear();
        }
        if (existingProduct.getVariants() != null) {
            existingProduct.getVariants().clear();
        }

        // ⚡ Flush để JPA thực thi DELETE trước khi INSERT
        productRepository.saveAndFlush(existingProduct);
        entityManager.clear();

        // ══ BƯỚC 2: Load lại fresh entity + category (sau khi clear context) ══
        final Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
        final Category freshCategory = getCategoryById(productRequest.getCategoryId());

        // ══ BƯỚC 3: Gán scalar fields ══
        product.setName(newData.getName());
        product.setSlug(productRequest.getSlug());
        product.setShortDescription(newData.getShortDescription());
        product.setDescription(newData.getDescription());
        product.setProductCode(newData.getProductCode());
        product.setSalePercentage(newData.getSalePercentage());
        product.setFeatured(newData.isFeatured());
        product.setActive(newData.isActive());
        product.setCategory(freshCategory);
        product.setTags(newData.getTags());

        // ══ BƯỚC 4: Gán child entities mới ══
        // Lưu ý: KHÔNG dùng set() với orphanRemoval=true, phải dùng addAll() trên collection gốc
        if (newData.getMainImage() != null) {
            newData.getMainImage().setProduct(product);
            product.setMainImage(newData.getMainImage());
        }

        if (newData.getSecondaryImages() != null && !newData.getSecondaryImages().isEmpty()) {
            newData.getSecondaryImages().forEach(img -> img.setProduct(product));
            if (product.getSecondaryImages() == null) {
                product.setSecondaryImages(newData.getSecondaryImages());
            } else {
                product.getSecondaryImages().addAll(newData.getSecondaryImages());
            }
        }

        if (newData.getVariants() != null && !newData.getVariants().isEmpty()) {
            newData.getVariants().forEach(v -> v.setProduct(product));
            if (product.getVariants() == null) {
                product.setVariants(newData.getVariants());
            } else {
                product.getVariants().addAll(newData.getVariants());
            }
        }

        Product savedProduct = productRepository.save(product);

        // ⚡ Xóa các ảnh cũ không còn tồn tại trong sản phẩm mới khỏi Cloudinary
        List<String> newPublicIds = collectPublicIds(savedProduct);
        List<String> idsToDelete = oldPublicIds.stream()
                .filter(idOld -> !newPublicIds.contains(idOld))
                .collect(Collectors.toList());
        cloudinaryService.deleteImages(idsToDelete);

        return toFullProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public boolean deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // ⚡ Xóa toàn bộ ảnh liên quan trên Cloudinary trước khi xóa mềm trong DB
        cloudinaryService.deleteImages(collectPublicIds(product));

        // Giải phóng unique constraints bằng cách đổi tên/mã
        String suffix = "_deleted_" + System.currentTimeMillis();
        product.setName(product.getName() + suffix);
        product.setSlug(product.getSlug() + suffix);
        product.setProductCode(product.getProductCode() + suffix);
        
        productRepository.save(product); // Lưu lại thông tin đã đổi tên
        productRepository.delete(product); // Hibernate sẽ thực hiện UPDATE deleted = true
        return true;
    }

    @Override
    public PageResponse<ProductResponse> searchProductsByName(String name, Pageable pageable) {
        Page<Product> products = productRepository.searchByKeyword(name, pageable);
        return toPageResponse(products);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "featured_products", key = "'all'")
    public PageResponse<ProductResponse> getFeaturedProducts(Pageable pageable) {
        log.info("Fetching featured products from DB (Cache Miss)");
        Page<Product> featured = productRepository.findByFeaturedTrue(pageable);
        return toPageResponse(featured);
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));
    }

    private Product createOrUpdateProductEntity(ProductRequest req, Category category, Long excludeId) {
        // Kiểm tra xem có sản phẩm nào (kể cả đã xóa) bị trùng mã/tên/slug không
        // Khi update (excludeId != null), bỏ qua chính sản phẩm đang cập nhật
        productRepository.findAnyByProductCode(req.getProductCode())
                .filter(p -> excludeId == null || !excludeId.equals(p.getId()))
                .ifPresent(this::cleanDeletedProduct);
        productRepository.findAnyByName(req.getName())
                .filter(p -> excludeId == null || !excludeId.equals(p.getId()))
                .ifPresent(this::cleanDeletedProduct);
        productRepository.findAnyBySlug(generateUniqueSlug(req.getName()))
                .filter(p -> excludeId == null || !excludeId.equals(p.getId()))
                .ifPresent(this::cleanDeletedProduct);

        Product product = productMapper.toProduct(req);
        product.setCategory(category);

        // 1. PRE-UPLOAD ALL IMAGES IN PARALLEL
        log.info("Starting parallel pre-upload for all product images");
        long startTime = System.currentTimeMillis();
        
        // Harvest all images
        java.util.List<MultipartFile> allFiles = new ArrayList<>();
        if (req.getMainImage() != null && !req.getMainImage().isEmpty()) allFiles.add(req.getMainImage());
        
        if (req.getSecondaryImages() != null) {
            for (MultipartFile f : req.getSecondaryImages()) {
                if (f != null && !f.isEmpty()) allFiles.add(f);
            }
        }
        
        if (req.getVariants() != null) {
            req.getVariants().forEach(v -> {
                if (v.getImage() != null && !v.getImage().isEmpty()) allFiles.add(v.getImage());
            });
        }

        // Upload everything in one big parallel burst
        List<ImageMetadata> allMetadata = cloudinaryService.uploadImages(allFiles);
        
        // Create an iterator to map them back in the same order they were added
        java.util.Iterator<ImageMetadata> metaIterator = allMetadata.iterator();
        log.info("Parallel upload of {} images completed in {} ms", allFiles.size(), (System.currentTimeMillis() - startTime));

        // 2. Map Image results back to Product
        // Main Image
        ImageMetadata mainM = (req.getMainImageMetadata() != null) ? req.getMainImageMetadata() 
                             : (req.getMainImage() != null && !req.getMainImage().isEmpty() && metaIterator.hasNext() ? metaIterator.next() : null);
        if (mainM != null) {
            ProductMainImage m = new ProductMainImage();
            m.setImageUrl(mainM.getUrl());
            m.setPublicId(mainM.getPublicId());
            m.setProduct(product);
            product.setMainImage(m);
        }

        // Secondary Images
        List<ImageMetadata> secList = new ArrayList<>();
        if (req.getSecondaryImageMetadata() != null) secList.addAll(req.getSecondaryImageMetadata());
        if (req.getSecondaryImages() != null) {
            for (MultipartFile f : req.getSecondaryImages()) {
                if (!f.isEmpty() && metaIterator.hasNext()) secList.add(metaIterator.next());
            }
        }
        product.setSecondaryImages(secList.stream().map(mM -> {
            ProductSecondaryImage s = new ProductSecondaryImage();
            s.setImageUrl(mM.getUrl());
            s.setPublicId(mM.getPublicId());
            s.setProduct(product);
            return s;
        }).collect(Collectors.toList()));

        // 3. Map Tags
        if (req.getTags() != null) {
            Set<Tag> tags = req.getTags().stream()
                    .map(name -> tagRepository.findByName(name)
                            .orElseGet(() -> tagRepository.save(new Tag(name))))
                    .collect(Collectors.toSet());
            product.setTags(tags);
        }

        // 4. Map Variants and Sizes
        if (req.getVariants() != null) {
            List<ProductVariant> variants = new ArrayList<>();
            java.util.Map<String, SizeProduct> localSizeCache = new java.util.HashMap<>();
            
            for (ProductVariantRequest vReq : req.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setColor(vReq.getColor());
                variant.setHexCode(vReq.getHexCode());
                variant.setProduct(product);

                // Variant Image
                ImageMetadata vM = (vReq.getImageMetadata() != null) ? vReq.getImageMetadata()
                                 : (vReq.getImage() != null && !vReq.getImage().isEmpty() && metaIterator.hasNext() ? metaIterator.next() : null);
                if (vM != null) {
                    variant.setImageUrl(vM.getUrl());
                    variant.setPublicId(vM.getPublicId());
                }

                // SMART SIZE MAPPING
                final List<SizeProductRequest> inputSizes = Optional.ofNullable(vReq.getSizes()).orElse(Collections.emptyList());
                List<SizeCategory> categorySizes = Optional.ofNullable(category.getSizeCategories()).orElse(Collections.emptyList());
                List<SizeProductVariant> sizeVariants = new ArrayList<>();

                for (int i = 0; i < categorySizes.size(); i++) {
                    SizeCategory sizeCategory = categorySizes.get(i);
                    String sName = sizeCategory.getName();

                    SizeProductRequest matchedInput = inputSizes.stream()
                            .filter(is -> is.getSizeName() != null && is.getSizeName().equalsIgnoreCase(sName))
                            .findFirst()
                            .orElse(null);

                    if (matchedInput == null && i < inputSizes.size()) {
                        SizeProductRequest indexedInput = inputSizes.get(i);
                        if (indexedInput.getSizeName() == null || indexedInput.getSizeName().isBlank()) matchedInput = indexedInput;
                    }

                    SizeProduct size = localSizeCache.get(sName);
                    if (size == null) {
                        size = sizeProductRepository.findBySizeName(sName)
                                .orElseGet(() -> {
                                    SizeProduct s = new SizeProduct();
                                    s.setSizeName(sName);
                                    return sizeProductRepository.saveAndFlush(s);
                                });
                        localSizeCache.put(sName, size);
                    }

                    // WATERFALL PRICING
                    BigDecimal resolvedPrice = (matchedInput != null && matchedInput.getPrice() != null) 
                            ? matchedInput.getPrice() 
                            : (vReq.getPrice() != null ? vReq.getPrice() : req.getPrice());
                    
                    SizeProductVariant spv = new SizeProductVariant();
                    spv.setProductVariant(variant);
                    spv.setSizeProduct(size);
                    spv.setPrice(resolvedPrice);
                    spv.setPriceAfterDiscount(calculatePriceAfterDiscount(resolvedPrice, req.getSalePercentage()));
                    spv.setStock(matchedInput != null ? matchedInput.getStock() : 0);
                    sizeVariants.add(spv);
                }
                variant.setProductVariantSizes(sizeVariants);
                variants.add(variant);
            }
            product.setVariants(variants);
        }

        return product;
    }

    private List<String> collectPublicIds(Product product) {
        List<String> ids = new ArrayList<>();
        if (product.getMainImage() != null && product.getMainImage().getPublicId() != null) {
            ids.add(product.getMainImage().getPublicId());
        }
        if (product.getSecondaryImages() != null) {
            product.getSecondaryImages().forEach(img -> {
                if (img.getPublicId() != null) ids.add(img.getPublicId());
            });
        }
        if (product.getDescriptionImages() != null) {
            product.getDescriptionImages().forEach(img -> {
                if (img.getPublicId() != null) ids.add(img.getPublicId());
            });
        }
        if (product.getVariants() != null) {
            product.getVariants().forEach(v -> {
                if (v.getPublicId() != null) ids.add(v.getPublicId());
            });
        }
        return ids;
    }


    private void cleanDeletedProduct(Product product) {
        if (product.isDeleted()) {
            String suffix = "_clr_" + System.currentTimeMillis();
            product.setProductCode(product.getProductCode() + suffix);
            product.setName(product.getName() + suffix);
            product.setSlug(product.getSlug() + suffix);
            productRepository.save(product);
            log.info("Cleaned up soft-deleted conflict for product code: {}", product.getProductCode());
        }
    }

    private BigDecimal calculatePriceAfterDiscount(BigDecimal price, Integer salePercentage) {
        if (price == null) return BigDecimal.ZERO;
        if (salePercentage == null || salePercentage <= 0) return price;
        BigDecimal discount = price.multiply(new BigDecimal(salePercentage)).divide(new BigDecimal(100));
        return price.subtract(discount);
    }

    private String generateUniqueSlug(String name) {
        String base = SlugUtils.toSlug(name);
        String slug = base;
        int count = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = base + "-" + count++;
        }
        return slug;
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

        return new PageResponse<>(
                page.getNumber(),
                page.getTotalPages(),
                page.getSize(),
                page.getTotalElements(),
                items
        );
    }

    private ProductResponse toFullProductResponse(Product product) {
        ProductResponse res = productMapper.toProductResponse(product);
        long reviewCount = productRepository.countReviewsByProductId(product.getId());
        res.setReviewCount((int) reviewCount);
        return res;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void seedDummyProducts(int count) {
        String[] catNames = {"Áo Thun", "Quần Jean", "Áo Khoác", "Sơ Mi", "Phụ Kiện", "Đồ Thể Thao"};
        List<Category> categories = new java.util.ArrayList<>();
        for (String cName : catNames) {
            String cSlug = SlugUtils.toSlug(cName);
            Category c = categoryRepository.findBySlug(cSlug);
            if (c == null) {
                c = categoryRepository.save(Category.builder().name(cName).slug(cSlug).build());
            }
            categories.add(c);
        }

        String[] sizeNames = {"S", "M", "L", "XL"};
        List<org.bbqqvv.backendecommerce.entity.SizeProduct> sizes = new java.util.ArrayList<>();
        for (String sName : sizeNames) {
            org.bbqqvv.backendecommerce.entity.SizeProduct sz = sizeProductRepository.findAll().stream()
                    .filter(s -> s.getSizeName().equals(sName)).findFirst()
                    .orElseGet(() -> sizeProductRepository.save(org.bbqqvv.backendecommerce.entity.SizeProduct.builder().sizeName(sName).price(java.math.BigDecimal.ZERO).priceAfterDiscount(java.math.BigDecimal.ZERO).build()));
            sizes.add(sz);
        }

        // 🔗 Link ALL Categories with Sizes in SizeCategory table to pass validation
        List<Category> allCategories = categoryRepository.findAll();
        for (Category cat : allCategories) {
            List<String> existingSizes = sizeCategoryRepository.findSizeNamesByCategoryId(cat.getId());
            for (String sName : sizeNames) {
                if (existingSizes.stream().noneMatch(s -> s.equalsIgnoreCase(sName))) {
                    sizeCategoryRepository.save(new SizeCategory(null, sName, cat));
                }
            }
        }
        sizeCategoryRepository.flush();
        entityManager.clear(); // Force re-fetch of categories with updated sizeCategories

        categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            log.error("❌ No categories found after linking sizes. Seeding aborted.");
            return;
        }

        String[] tagNames = {"Mới", "Hot", "Bán Chạy", "Săn Sale", "Trend 2024"};
        List<Tag> tags = new java.util.ArrayList<>();
        for (String tName : tagNames) {
            Tag t = tagRepository.findByName(tName).orElseGet(() -> tagRepository.save(new Tag(tName)));
            tags.add(t);
        }

        String[] prefixes = {"Áo Thun Cổ Tròn", "Quần Kaki Túi Hộp", "Áo Sơ Mi Lụa", "Áo Khoác Bomber", "Quần Jean Rách", "Phụ Kiện Voan", "Áo Len Hình Gấu", "Set Đồ Thể Thao"};
        String[] colors = {"#000000", "#FFFFFF", "#1E90FF", "#FFB6C1", "#8FBC8F"};

        List<Product> productsToSave = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();

        for (int i = 1; i <= count; i++) {
            String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 5);
            Category randomCat = categories.get(random.nextInt(categories.size()));
            String pName = prefixes[random.nextInt(prefixes.length)] + " " + uniqueSuffix.toUpperCase();
            
            org.bbqqvv.backendecommerce.dto.request.ProductRequest req = new org.bbqqvv.backendecommerce.dto.request.ProductRequest();
            req.setName(pName);
            req.setProductCode("SEED-" + uniqueSuffix.toUpperCase() + i);
            req.setShortDescription("Sản phẩm " + pName + " chất lượng cao.");
            req.setDescription("<h3>Chi tiết</h3><p>Mô tả mẫu cho sản phẩm chất lượng cao.</p>");
            req.setPrice(new BigDecimal("200000"));
            req.setSalePercentage(random.nextInt(3) * 10);
            req.setCategoryId(randomCat.getId());
            req.setFeatured(random.nextInt(100) < 30);
            req.setSlug(SlugUtils.toSlug(pName) + "-" + uniqueSuffix);
            
            // Image Metadata
            org.bbqqvv.backendecommerce.dto.request.ImageMetadata mainM = new org.bbqqvv.backendecommerce.dto.request.ImageMetadata(
                "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg", "sample_public_id"
            );
            req.setMainImageMetadata(mainM);

            List<org.bbqqvv.backendecommerce.dto.request.ProductVariantRequest> vReqs = new ArrayList<>();
            int colorCount = 1 + random.nextInt(2);
            for (int c = 0; c < colorCount; c++) {
                String colorHex = colors[(random.nextInt(colors.length))];
                org.bbqqvv.backendecommerce.dto.request.ProductVariantRequest v = new org.bbqqvv.backendecommerce.dto.request.ProductVariantRequest();
                v.setColor(colorHex);
                v.setHexCode(colorHex);
                v.setImageMetadata(mainM);
                
                List<org.bbqqvv.backendecommerce.dto.request.SizeProductRequest> sReqs = new ArrayList<>();
                for (String sName : sizeNames) {
                    org.bbqqvv.backendecommerce.dto.request.SizeProductRequest s = new org.bbqqvv.backendecommerce.dto.request.SizeProductRequest();
                    s.setSizeName(sName);
                    s.setStock(50 + random.nextInt(50));
                    s.setPrice(req.getPrice());
                    sReqs.add(s);
                }
                v.setSizes(sReqs);
                vReqs.add(v);
            }
            req.setVariants(vReqs);
            
            Product p = createOrUpdateProductEntity(req, randomCat, null);
            p.setSlug(req.getSlug());
            productsToSave.add(p);
        }
        productRepository.saveAllAndFlush(productsToSave);
        log.info("✅ Successfully seeded {} products using official creation flow.", count);
    }
}

