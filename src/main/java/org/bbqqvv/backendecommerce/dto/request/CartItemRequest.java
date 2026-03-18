package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequest {
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    private String sizeName;

    private String color;
}
