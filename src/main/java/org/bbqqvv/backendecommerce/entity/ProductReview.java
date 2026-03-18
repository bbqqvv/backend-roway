package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"product", "user", "orderItem", "images"})
@EqualsAndHashCode(callSuper = false, exclude = {"product", "user", "orderItem", "images"})
public class ProductReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @OneToMany(mappedBy = "productReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductReviewImage> images = new ArrayList<>();

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 1000)
    private String reviewText;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

