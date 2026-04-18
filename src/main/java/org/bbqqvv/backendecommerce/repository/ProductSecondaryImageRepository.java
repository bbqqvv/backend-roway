package org.bbqqvv.backendecommerce.repository;

import org.bbqqvv.backendecommerce.entity.ProductSecondaryImage;
import org.bbqqvv.backendecommerce.entity.ImageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductSecondaryImageRepository extends JpaRepository<ProductSecondaryImage, Long> {
    List<ProductSecondaryImage> findByDraftId(String draftId);
    List<ProductSecondaryImage> findByStatusAndCreatedAtBefore(ImageStatus status, LocalDateTime dateTime);
    boolean existsByPublicId(String publicId);
    Optional<ProductSecondaryImage> findByPublicId(String publicId);
    List<ProductSecondaryImage> findAllByPublicId(String publicId);
}
