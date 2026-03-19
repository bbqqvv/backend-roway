package org.bbqqvv.backendecommerce.repository;

import jakarta.transaction.Transactional;
import org.bbqqvv.backendecommerce.entity.DiscountProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;

public interface DiscountProductRepository extends JpaRepository<DiscountProduct, Long> {
    void deleteByDiscountId(Long discountId);
    List<DiscountProduct> findByDiscountId(Long discountId);

    @Query("SELECT dp.product.id FROM DiscountProduct dp WHERE dp.discount.id = :discountId")
    Page<Long> findProductIdsByDiscountId(@Param("discountId") Long discountId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM DiscountProduct dp WHERE dp.discount.id = :discountId AND dp.product.id IN :productIds")
    void deleteByDiscountIdAndProductIds(@Param("discountId") Long discountId, @Param("productIds") Set<Long> productIds);
}
