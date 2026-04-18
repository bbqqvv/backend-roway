package org.bbqqvv.backendecommerce.repository;

import org.bbqqvv.backendecommerce.entity.ProductVariant;
import org.bbqqvv.backendecommerce.entity.ImageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByDraftId(String draftId);
    List<ProductVariant> findByStatusAndCreatedAtBefore(ImageStatus status, LocalDateTime dateTime);
    boolean existsByPublicId(String publicId);
    Optional<ProductVariant> findByPublicId(String publicId);
    List<ProductVariant> findAllByPublicId(String publicId);
}
