package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE orders SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@ToString(exclude = {"orderItems"})
@EqualsAndHashCode(callSuper = false, exclude = {"orderItems"})
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String recipientName;

    @Column(name = "order_code", unique = true, nullable = false, length = 20)
    private String orderCode;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(name = "full_address", nullable = false)
    private String fullAddress;

    @Column(nullable = true)
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "discount_id", nullable = true)
    private Discount discount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "discount_code", length = 50, nullable = true)
    private String discountCode;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = true)
    private BigDecimal shippingFee;

    @Column(nullable = true)
    private LocalDate expectedDeliveryDate;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
}

