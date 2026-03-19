package org.bbqqvv.backendecommerce.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"applicableProducts", "applicableUsers"})
@EqualsAndHashCode(callSuper = false, exclude = {"applicableProducts", "applicableUsers"})
public class Discount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // Mã giảm giá

    @Column(nullable = false)
    private BigDecimal discountAmount; // Số tiền hoặc % giảm giá

    @Column
    private BigDecimal maxDiscountAmount; // Giới hạn tối đa cho giảm giá %

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // Loại giảm giá (CỐ ĐỊNH hoặc %)

    @Column(nullable = false)
    private BigDecimal minOrderValue; // Giá trị đơn hàng tối thiểu để áp dụng

    @Column(nullable = false)
    private Integer usageLimit; // Số lần sử dụng tối đa

    @Column(nullable = false)
    private Integer timesUsed = 0; // Số lần đã sử dụng

    @Column(nullable = false)
    private LocalDateTime startDate; // Ngày bắt đầu áp dụng

    @Column(nullable = false)
    private LocalDateTime expiryDate; // Ngày hết hạn

    @Column(nullable = false)
    private boolean active = true; // Trạng thái hoạt động của mã giảm giá

    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscountProduct> applicableProducts = new ArrayList<>();

    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscountUser> applicableUsers = new ArrayList<>();

    // 🔹 Kiểm tra xem mã giảm giá có còn hiệu lực không
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    // 🔹 Kiểm tra xem mã giảm giá đã đạt giới hạn sử dụng chưa
    public boolean isUsageLimitReached() {
        return timesUsed >= usageLimit;
    }

    // 🔹 Kiểm tra xem mã giảm giá có thể áp dụng cho người dùng không
    // Nếu danh sách trống -> Mã công khai (Global)
    public boolean isApplicableForUser(User user) {
        if (applicableUsers == null || applicableUsers.isEmpty()) {
            return true;
        }
        if (user == null) return false;
        return applicableUsers.stream()
                .anyMatch(discountUser -> discountUser.getUser().getId().equals(user.getId()));
    }

    // 🔹 Kiểm tra xem mã giảm giá có áp dụng cho sản phẩm này không
    // Nếu danh sách trống -> Áp dụng cho mọi sản phẩm
    public boolean isApplicableForProduct(Product product) {
        if (applicableProducts == null || applicableProducts.isEmpty()) {
            return true;
        }
        return applicableProducts.stream()
                .anyMatch(dp -> dp.getProduct().getId().equals(product.getId()));
    }
}


