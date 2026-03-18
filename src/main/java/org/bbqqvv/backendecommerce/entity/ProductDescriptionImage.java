package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "product_description_images")  // Đặt tên bảng là product_description_images
public class ProductDescriptionImage extends BaseEntity implements ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Tự động tăng id
    @Column(name = "id")  // Cột id
    private Long id;

    @ManyToOne  // Nhiều ảnh mô tả có thể liên kết với một sản phẩm
    @JoinColumn(name = "product_id", nullable = true)  // Liên kết với bảng Product, cho phép null
    private Product product;

    @Column(name = "image_url", nullable = false)  // Cột chứa URL của ảnh mô tả
    private String imageUrl;
}

