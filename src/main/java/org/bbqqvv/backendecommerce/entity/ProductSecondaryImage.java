package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity@Table(name = "product_secondary_images")
public class ProductSecondaryImage extends BaseEntity implements ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    public ProductSecondaryImage(String imageUrl, Product product) {
        this.imageUrl = imageUrl;
        this.product = product;
    }
}

