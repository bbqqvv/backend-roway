package org.bbqqvv.backendecommerce.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.entity.ImageStatus;
import org.bbqqvv.backendecommerce.repository.ProductMainImageRepository;
import org.bbqqvv.backendecommerce.repository.ProductSecondaryImageRepository;
import org.bbqqvv.backendecommerce.repository.ProductVariantRepository;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageCleanupTask {

    private final ProductMainImageRepository mainImageRepository;
    private final ProductSecondaryImageRepository secondaryImageRepository;
    private final ProductVariantRepository variantRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Chạy mỗi giờ để dọn dẹp các ảnh TEMP quá 24h
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupTempImages() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        log.info("Starting cleanup of TEMP images created before {}", threshold);

        List<String> publicIdsToDelete = new ArrayList<>();

        // 1. Cleanup Main Images
        mainImageRepository.findByStatusAndCreatedAtBefore(ImageStatus.TEMP, threshold)
                .forEach(img -> {
                    if (img.getPublicId() != null) publicIdsToDelete.add(img.getPublicId());
                    mainImageRepository.delete(img);
                });

        // 2. Cleanup Secondary Images
        secondaryImageRepository.findByStatusAndCreatedAtBefore(ImageStatus.TEMP, threshold)
                .forEach(img -> {
                    if (img.getPublicId() != null) publicIdsToDelete.add(img.getPublicId());
                    secondaryImageRepository.delete(img);
                });

        // 3. Cleanup Variants
        variantRepository.findByStatusAndCreatedAtBefore(ImageStatus.TEMP, threshold)
                .forEach(v -> {
                    if (v.getPublicId() != null) publicIdsToDelete.add(v.getPublicId());
                    variantRepository.delete(v);
                });

        // 4. Batch delete from Cloudinary
        if (!publicIdsToDelete.isEmpty()) {
            log.info("Deleting {} orphaned images from Cloudinary (Job 1)", publicIdsToDelete.size());
            cloudinaryService.deleteImages(publicIdsToDelete);
        }
    }

    /**
     * Job 2: Quét folder Cloudinary để xóa các file không có trong DB (orphaned files)
     * Chạy mỗi ngày một lần vào lúc 3h sáng để tránh quá tải Admin API
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOrphanedCloudinaryFiles() {
        String prefix = "products/tmp/";
        log.info("Starting Job 2: Scanning Cloudinary prefix {} for orphaned files", prefix);

        // Lấy tài nguyên kèm metadata (created_at)
        java.util.Map<String, java.time.LocalDateTime> cloudMeta = cloudinaryService.listResourcesWithMetadata(prefix);
        List<String> toDelete = new ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        // Grace Period: 2 giờ (Để tránh xóa nhầm ảnh đang trong quá trình upload/register)
        long gracePeriodHours = 2;

        for (java.util.Map.Entry<String, java.time.LocalDateTime> entry : cloudMeta.entrySet()) {
            String publicId = entry.getKey();
            java.time.LocalDateTime createdAt = entry.getValue();

            // Chỉ xét các file đã tồn tại lâu hơn grace period
            if (createdAt.plusHours(gracePeriodHours).isBefore(now)) {
                boolean existsInDb = mainImageRepository.existsByPublicId(publicId) ||
                                     secondaryImageRepository.existsByPublicId(publicId) ||
                                     variantRepository.existsByPublicId(publicId);
                
                if (!existsInDb) {
                    toDelete.add(publicId);
                }
            }
        }

        if (!toDelete.isEmpty()) {
            log.info("Job 2: Found {} orphaned files on Cloudinary (older than {}h). Deleting...", toDelete.size(), gracePeriodHours);
            cloudinaryService.deleteImages(toDelete);
        }
    }
}
