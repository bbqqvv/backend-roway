package org.bbqqvv.backendecommerce.repository;

import jakarta.transaction.Transactional;
import org.bbqqvv.backendecommerce.entity.Discount;
import org.bbqqvv.backendecommerce.entity.DiscountUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DiscountUserRepository extends JpaRepository<DiscountUser, Long> {

    void deleteByDiscountId(Long discountId);

    List<DiscountUser> findByDiscountId(Long discountId);

    @Query("SELECT du.user.id FROM DiscountUser du WHERE du.discount.id = :discountId")
    Page<Long> findUserIdsByDiscountId(@Param("discountId") Long discountId, Pageable pageable);
    @Query("SELECT du.discount.code FROM DiscountUser du WHERE du.user.id = :userId")
    List<String> findDiscountCodesByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DiscountUser du WHERE du.discount.id = :discountId AND du.user.id IN :userIds")
    void deleteByDiscountIdAndUserIds(@Param("discountId") Long discountId, @Param("userIds") Set<Long> userIds);

    @Query("SELECT du.discount FROM DiscountUser du WHERE du.user.id = :userId")
    Page<Discount> findDiscountsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(du) > 0 FROM DiscountUser du WHERE du.discount.id = :discountId AND du.user.id = :userId")
    boolean existsByDiscountIdAndUserId(@Param("discountId") Long discountId, @Param("userId") Long userId);

    boolean existsByUserIdAndDiscountCode(Long id, String discountCode);
}
