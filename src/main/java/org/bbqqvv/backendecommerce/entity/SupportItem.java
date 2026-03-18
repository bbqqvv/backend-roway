package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "support_items") // Lưu ý bỏ khoảng trắng dư thừa trong tên bảng
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class SupportItem extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động tăng ID
    private Long id;

    @Column(name = "img", nullable = false) // Đường dẫn ảnh
    private String img;

    @Column(name = "title", nullable = false) // Tiêu đề hỗ trợ
    private String title;

    @Column(name = "hours", nullable = false) // Giờ làm việc
    private String hours;

    @Column(name = "contact", nullable = false) // Số điện thoại hoặc email
    private String contact;

    @Column(name = "link", nullable = false) // Link liên hệ
    private String link;

    @Column(name = "bgColor", nullable = false) // Màu nền
    private String bgColor;
}
