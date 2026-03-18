package org.bbqqvv.backendecommerce.repository;

import org.bbqqvv.backendecommerce.entity.SizeProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SizeProductVariantRepository extends JpaRepository<SizeProductVariant, Long> {
    @Modifying
    @Query("UPDATE SizeProductVariant spv SET spv.stock = spv.stock - :quantity WHERE spv.id = :id")
    void reduceStock(@Param("id") Long id, @Param("quantity") int quantity);
    @Query("SELECT spv FROM SizeProductVariant spv " +
            "JOIN FETCH spv.productVariant pv " +
            "JOIN FETCH spv.sizeProduct sp " +
            "WHERE pv.product.id IN :productIds")
    java.util.List<SizeProductVariant> findByProductIdIn(@Param("productIds") java.util.Collection<Long> productIds);

    @Query("SELECT spv FROM SizeProductVariant spv " +
            "JOIN spv.productVariant pv " +
            "JOIN spv.sizeProduct sp " +
            "WHERE pv.product.id = :productId AND sp.sizeName = :sizeName")
    Optional<SizeProductVariant> findByProductIdAndSizeName(@Param("productId") Long productId, @Param("sizeName") String sizeName);

}
