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
public class ProductMainImage extends BaseEntity implements ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Tự động tăng id
    @Column(name = "id")  // Cột id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)  // Liên kết với bảng Product
    @JoinColumn(name = "product_id", nullable = false)  // Liên kết với bảng Product
    private Product product;

    @Column(name = "image_url", nullable = false)  // Cột chứa URL của ảnh chính
    private String imageUrl;
}

