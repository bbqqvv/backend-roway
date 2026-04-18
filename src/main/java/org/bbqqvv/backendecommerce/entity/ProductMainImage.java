package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "product_main_images")  // Đặt tên bảng là product_main_images
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMainImage extends BaseEntity implements StageableImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Tự động tăng id
    @Column(name = "id")  // Cột id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ImageStatus status = ImageStatus.TEMP;

    @Column(name = "draft_id")
    private String draftId;

    @Column(name = "image_url", nullable = false)  // Cột chứa URL của ảnh chính
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
}

