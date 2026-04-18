package org.bbqqvv.backendecommerce.repository;

import org.bbqqvv.backendecommerce.entity.ProductMainImage;
import org.bbqqvv.backendecommerce.entity.ImageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductMainImageRepository extends JpaRepository<ProductMainImage, Long> {
    List<ProductMainImage> findByDraftId(String draftId);
    List<ProductMainImage> findByStatusAndCreatedAtBefore(ImageStatus status, LocalDateTime dateTime);
    boolean existsByPublicId(String publicId);
    Optional<ProductMainImage> findByPublicId(String publicId);
}
