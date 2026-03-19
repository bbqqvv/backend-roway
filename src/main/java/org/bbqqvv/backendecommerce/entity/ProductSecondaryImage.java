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

    @Column(name = "public_id")
    private String publicId;

    @Override
    public String getImageUrl() {
        return this.imageUrl;
    }

    @Override
    public String getPublicId() {
        return this.publicId;
    }

    public ProductSecondaryImage(String imageUrl, Product product) {
        this.imageUrl = imageUrl;
        this.product = product;
    }
}

