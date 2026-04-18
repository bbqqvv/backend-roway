package org.bbqqvv.backendecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.bbqqvv.backendecommerce.dto.request.ImageRegisterRequest;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.repository.ProductMainImageRepository;
import org.bbqqvv.backendecommerce.repository.ProductSecondaryImageRepository;
import org.bbqqvv.backendecommerce.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductMainImageRepository mainImageRepository;
    private final ProductSecondaryImageRepository secondaryImageRepository;
    private final ProductVariantRepository variantRepository;
    private final org.bbqqvv.backendecommerce.service.img.CloudinaryService cloudinaryService;

    @Transactional
    public void unregisterImage(String publicId) {
        log.info("Unregistering image (Deferred Deletion) with publicId: {}", publicId);
        
        List<ProductMainImage> mains = mainImageRepository.findAllByPublicId(publicId);
        List<ProductSecondaryImage> secondaries = secondaryImageRepository.findAllByPublicId(publicId);
        List<ProductVariant> variants = variantRepository.findAllByPublicId(publicId);

        boolean found = !mains.isEmpty() || !secondaries.isEmpty() || !variants.isEmpty();

        if (found) {
            // Case 8: Only delete from DB. Let the scheduler handle Cloudinary later.
            // This provides a "Grace Period" (Recycle Bin) for accidental deletions.
            if (!mains.isEmpty()) mainImageRepository.deleteAll(mains);
            if (!secondaries.isEmpty()) secondaryImageRepository.deleteAll(secondaries);
            if (!variants.isEmpty()) variantRepository.deleteAll(variants);
            
            log.info("DB records removed for {}. Cloudinary file remains for grace period.", publicId);
        }
    }

    @Transactional
    public void registerImage(ImageRegisterRequest request) {
        if (request.getPublicId() == null || request.getPublicId().isBlank()) return;

        // Prevent duplicate registration
        if (mainImageRepository.existsByPublicId(request.getPublicId()) ||
            secondaryImageRepository.existsByPublicId(request.getPublicId()) ||
            variantRepository.existsByPublicId(request.getPublicId())) {
            log.info("Image {} already registered, skipping.", request.getPublicId());
            return;
        }

        log.info("Registering {} image with draftId: {}", request.getType(), request.getDraftId());

        StageableImage image = createPlaceholder(request.getType());
        image.setDraftId(request.getDraftId());
        image.setImageUrl(request.getUrl());
        image.setPublicId(request.getPublicId());
        image.setStatus(ImageStatus.TEMP);

        saveImage(image);
    }

    @Transactional
    public void registerImagesBatch(List<ImageRegisterRequest> requests) {
        if (requests == null || requests.isEmpty()) return;
        log.info("Registering batch of {} images", requests.size());
        requests.forEach(this::registerImage);
    }

    private StageableImage createPlaceholder(String type) {
        return switch (type.toUpperCase()) {
            case "MAIN" -> new ProductMainImage();
            case "SECONDARY" -> new ProductSecondaryImage();
            case "VARIANT" -> {
                ProductVariant variant = new ProductVariant();
                variant.setColor("PENDING");
                yield variant;
            }
            default -> throw new IllegalArgumentException("Unknown image type: " + type);
        };
    }

    private void saveImage(StageableImage image) {
        if (image instanceof ProductMainImage i) mainImageRepository.save(i);
        else if (image instanceof ProductSecondaryImage i) secondaryImageRepository.save(i);
        else if (image instanceof ProductVariant i) variantRepository.save(i);
    }

    @Transactional
    public void activateImages(String draftId, Product product) {
        if (draftId == null || draftId.isBlank()) return;
        log.info("Activating images for draftId: {} and product: {}", draftId, product.getId());

        // 1. Activate Main Image
        mainImageRepository.findByDraftId(draftId).forEach(img -> {
            img.setStatus(ImageStatus.ACTIVE);
            img.setProduct(product);
            product.setMainImage(img);
            mainImageRepository.save(img);
        });

        // 2. Activate Secondary Images
        List<ProductSecondaryImage> secondaryImages = secondaryImageRepository.findByDraftId(draftId);
        secondaryImages.forEach(img -> {
            img.setStatus(ImageStatus.ACTIVE);
            img.setProduct(product);
            secondaryImageRepository.save(img);
        });
        
        if (product.getSecondaryImages() == null) {
            product.setSecondaryImages(new java.util.ArrayList<>(secondaryImages));
        } else {
            product.getSecondaryImages().addAll(secondaryImages);
        }
    }

    @Transactional(readOnly = true)
    public List<org.bbqqvv.backendecommerce.dto.request.ImageMetadata> getImagesByDraftId(String draftId) {
        List<org.bbqqvv.backendecommerce.dto.request.ImageMetadata> results = new java.util.ArrayList<>();
        
        mainImageRepository.findByDraftId(draftId).forEach(img -> 
            results.add(new org.bbqqvv.backendecommerce.dto.request.ImageMetadata(img.getImageUrl(), img.getPublicId())));
            
        secondaryImageRepository.findByDraftId(draftId).forEach(img -> 
            results.add(new org.bbqqvv.backendecommerce.dto.request.ImageMetadata(img.getImageUrl(), img.getPublicId())));
            
        variantRepository.findByDraftId(draftId).forEach(img -> 
            results.add(new org.bbqqvv.backendecommerce.dto.request.ImageMetadata(img.getImageUrl(), img.getPublicId())));
            
        return results;
    }

    @Transactional
    public void cleanupStaleImages(java.time.LocalDateTime beforeTime, org.bbqqvv.backendecommerce.service.img.CloudinaryService cloudinaryService) {
        log.info("Execution of cleanupStaleImages triggered at {}", java.time.LocalDateTime.now());

        cleanupFromRepo(mainImageRepository, ImageStatus.TEMP, beforeTime, cloudinaryService);
        cleanupFromRepo(secondaryImageRepository, ImageStatus.TEMP, beforeTime, cloudinaryService);
        cleanupFromRepo(variantRepository, ImageStatus.TEMP, beforeTime, cloudinaryService);
    }

    private <T extends StageableImage> void cleanupFromRepo(
            org.springframework.data.jpa.repository.JpaRepository<T, Long> repo,
            ImageStatus status,
            java.time.LocalDateTime beforeTime,
            org.bbqqvv.backendecommerce.service.img.CloudinaryService cloudinaryService) {
        
        // Note: Assuming repositories have findByStatusAndCreatedAtBefore. 
        // If not, we might need a common interface for repositories or custom query.
        // For now, I'll stick to the specific repository calls to ensure safety.
        
        List<T> staleImages;
        if (repo instanceof ProductMainImageRepository r) staleImages = (List<T>) r.findByStatusAndCreatedAtBefore(status, beforeTime);
        else if (repo instanceof ProductSecondaryImageRepository r) staleImages = (List<T>) r.findByStatusAndCreatedAtBefore(status, beforeTime);
        else if (repo instanceof ProductVariantRepository r) staleImages = (List<T>) r.findByStatusAndCreatedAtBefore(status, beforeTime);
        else return;

        for (T img : staleImages) {
            try {
                log.info("Cleaning up stale image: {}", img.getPublicId());
                cloudinaryService.deleteImage(img.getPublicId());
                repo.delete(img);
            } catch (Exception e) {
                log.error("Failed to cleanup image {}: {}", img.getPublicId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void syncCloudinaryOrphans(java.time.LocalDateTime beforeTime) {
        log.info("Case 2: Syncing Cloudinary ghost assets (orphans) in products/tmp/ folder...");
        
        // List all resources in the temporary folder
        java.util.Map<String, java.time.LocalDateTime> cloudinaryResources = cloudinaryService.listResourcesWithMetadata("products/tmp/");
        
        for (java.util.Map.Entry<String, java.time.LocalDateTime> entry : cloudinaryResources.entrySet()) {
            String publicId = entry.getKey();
            java.time.LocalDateTime createdAt = entry.getValue();

            // Only process if it's older than our grace period (e.g. 24h)
            if (createdAt.isBefore(beforeTime)) {
                // Check if it exists in our DB
                boolean existsInDb = mainImageRepository.existsByPublicId(publicId) || 
                                     secondaryImageRepository.existsByPublicId(publicId) || 
                                     variantRepository.existsByPublicId(publicId);

                if (!existsInDb) {
                    log.info("Deleting orphan/ghost asset from Cloudinary: {} (Created at {})", publicId, createdAt);
                    try {
                        cloudinaryService.deleteImage(publicId);
                    } catch (Exception e) {
                        log.error("Failed to delete orphan asset {}: {}", publicId, e.getMessage());
                    }
                }
            }
        }
    }
}
