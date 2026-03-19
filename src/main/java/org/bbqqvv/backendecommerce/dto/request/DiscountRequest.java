package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.bbqqvv.backendecommerce.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class DiscountRequest {
    @NotBlank(message = "Discount code is required")
    private String code;

    @NotNull(message = "Discount amount is required")
    @Positive(message = "Discount amount must be positive")
    private BigDecimal discountAmount;

    @PositiveOrZero(message = "Max discount amount must be zero or positive")
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @PositiveOrZero(message = "Min order value must be zero or positive")
    private BigDecimal minOrderValue;

    @NotNull(message = "Usage limit is required")
    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDateTime expiryDate;

    private boolean active;
    private List<Long> applicableProducts;
    private List<Long> applicableUsers;
}
