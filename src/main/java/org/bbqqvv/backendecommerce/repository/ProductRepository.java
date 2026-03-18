package org.bbqqvv.backendecommerce.repository;


import org.bbqqvv.backendecommerce.entity.Category;
import org.bbqqvv.backendecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByProductCode(String productCode);
    @EntityGraph(attributePaths = {"category", "mainImage"})
    Page<Product> findProductByCategory(Category category, Pageable pageable);
    boolean existsBySlug(String slug);
    @EntityGraph(attributePaths = {"category", "mainImage"})
    Optional<Product> findBySlug(String slug);
    @EntityGraph(attributePaths = {"category", "mainImage"})
    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p.slug FROM Product p WHERE p.slug LIKE :basePattern")
    List<String> findSlugsByPattern(@Param("basePattern") String basePattern);

    @EntityGraph(attributePaths = {"category", "mainImage"})
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId")
    long countReviewsByProductId(@Param("productId") Long productId);
    @Query("SELECT DISTINCT pv.color FROM ProductVariant pv")
    List<String> findDistinctColors();
    @Query("SELECT DISTINCT sp.sizeName FROM SizeProduct sp")
    List<String> findDistinctSizes();
    @Query("SELECT DISTINCT t.name FROM Product p JOIN p.tags t")
    List<String> findDistinctTags();
    @Query("SELECT MIN(sp.price) FROM SizeProduct sp")
    BigDecimal findMinPrice();
    @Query("SELECT MAX(sp.price) FROM SizeProduct sp")
    BigDecimal findMaxPrice();
    @Query("SELECT r.product.id, COUNT(r) FROM ProductReview r WHERE r.product.id IN :productIds GROUP BY r.product.id")
    List<Object[]> countReviewsByProductIds(@Param("productIds") List<Long> productIds);
    
    @EntityGraph(attributePaths = {"category", "mainImage"})
    Page<Product> findByFeaturedTrue(Pageable pageable);
}
