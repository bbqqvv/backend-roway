package org.bbqqvv.backendecommerce.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
@ToString(exclude = {"order"})
@EqualsAndHashCode(callSuper = false, exclude = {"order"})
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Một đơn hàng có thể có nhiều lần thanh toán
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private BigDecimal amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_id") // Lưu ID giao dịch từ VNPay, Momo...
    private String transactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
}


