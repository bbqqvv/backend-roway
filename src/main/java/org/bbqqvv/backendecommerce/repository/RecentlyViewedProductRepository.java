package org.bbqqvv.backendecommerce.repository;

import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.entity.RecentlyViewedProduct;
import org.bbqqvv.backendecommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentlyViewedProductRepository extends JpaRepository<RecentlyViewedProduct, Long> {
    RecentlyViewedProduct findTop1ByUserAndProductOrderByUpdatedAtDesc(User currentUser, Product product);
    Page<RecentlyViewedProduct> findByUserOrderByUpdatedAtDesc(User currentUser, Pageable pageable);
    
    long countByUser(User user);
    void deleteAllByUser(User user);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = 
        "DELETE FROM recently_viewed_products WHERE id IN (" +
        "SELECT id FROM recently_viewed_products WHERE user_id = :userId " +
        "ORDER BY updated_at ASC LIMIT :count)", nativeQuery = true)
    void deleteOldestByUserId(@org.springframework.data.repository.query.Param("userId") Long userId, 
                               @org.springframework.data.repository.query.Param("count") int count);
}
