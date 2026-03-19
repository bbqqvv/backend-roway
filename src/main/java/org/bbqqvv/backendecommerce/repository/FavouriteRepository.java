package org.bbqqvv.backendecommerce.repository;

import io.micrometer.common.lang.NonNull;
import org.bbqqvv.backendecommerce.entity.Favourite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    Page<Favourite> findByUserId(Long userId, Pageable pageable);
    @NonNull
    Optional<Favourite> findById(Long id);

    boolean existsByProductIdAndUserId(Long productId, Long id);
    boolean existsByUserIdAndProductIdAndSizeProductVariantId(Long userId, Long productId, Long spvId);
}
