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
}
