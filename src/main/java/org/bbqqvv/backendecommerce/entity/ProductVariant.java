package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"productVariantSizes"})
@EqualsAndHashCode(callSuper = false, exclude = {"productVariantSizes"})
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SizeProductVariant> productVariantSizes;

    @Column(nullable = true)
    private String imageUrl;

    @Column(nullable = false)
    private String color;
}

