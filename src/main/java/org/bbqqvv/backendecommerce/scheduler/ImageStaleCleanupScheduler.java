package org.bbqqvv.backendecommerce.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.service.ProductImageService;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduler to cleanup stale draft images that were never converted to ACTIVE.
 * Follows the "Production-ready" architecture.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ImageStaleCleanupScheduler {

    private final ProductImageService productImageService;
    private final CloudinaryService cloudinaryService;

    // Run every 12 hours (43200000 ms)
    @Scheduled(fixedRate = 43200000)
    public void cleanupStaleImages() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        long startTime = System.currentTimeMillis();
        log.info("Execution of ImageStaleCleanupScheduler triggered at {}", threshold);
        
        try {
            // Case 1 & 3: Cleanup stale DB records (and their assets)
            productImageService.cleanupStaleImages(threshold, cloudinaryService);
            
            // Case 2 & 8: Cleanup "Ghost" assets (Cloudinary files with no DB record)
            productImageService.syncCloudinaryOrphans(threshold);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Execution of ImageStaleCleanupScheduler completed successfully in {} ms", duration);
        } catch (Exception e) {
            log.error("Execution of ImageStaleCleanupScheduler failed after {} ms: {}", (System.currentTimeMillis() - startTime), e.getMessage(), e);
        }
    }
}
