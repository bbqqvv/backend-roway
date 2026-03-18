package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "product_review_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class ProductReviewImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private ProductReview productReview;

    @Column(nullable = false, length = 500)
    private String imageUrl;
}
