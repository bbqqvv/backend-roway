package org.bbqqvv.backendecommerce.dto.response;

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
public class DiscountResponse {
    private Long id;
    private String code;
    private BigDecimal discountAmount;
    private BigDecimal maxDiscountAmount;
    private DiscountType discountType;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private Integer timesUsed;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private boolean active;
    private int applicableProductsCount;
    private int applicableUsersCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
