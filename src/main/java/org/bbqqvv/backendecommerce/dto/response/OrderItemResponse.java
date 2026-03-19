package org.bbqqvv.backendecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    Long productId;
    String productName;
    String mainImageUrl;
    String color;
    String sizeName;
    Integer quantity;
    BigDecimal price;
    BigDecimal subtotal;
}
