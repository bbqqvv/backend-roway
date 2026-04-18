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
public class ProductVariant extends BaseEntity implements StageableImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ImageStatus status = ImageStatus.TEMP;

    @Column(name = "draft_id")
    private String draftId;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SizeProductVariant> productVariantSizes;

    @Column(nullable = true)
    private String imageUrl;

    @Column(name = "public_id")
    private String publicId;

    @Column(nullable = false)
    private String color;

    @Column(name = "hex_code")
    private String hexCode;
}

