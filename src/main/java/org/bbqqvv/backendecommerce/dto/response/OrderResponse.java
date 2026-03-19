package org.bbqqvv.backendecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bbqqvv.backendecommerce.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    Long userId;
    String name;
    String address;
    String phoneNumber;
    String notes;
    String orderCode;
    String status;
    PaymentMethod paymentMethod;
    BigDecimal shippingFee;
    String discountCode;
    BigDecimal discountAmount;
    LocalDate expectedDeliveryDate;
    List<OrderItemResponse> orderItems;
    BigDecimal totalAmount;
    String paymentUrl;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
