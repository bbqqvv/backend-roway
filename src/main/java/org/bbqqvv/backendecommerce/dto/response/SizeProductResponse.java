package org.bbqqvv.backendecommerce.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeProductResponse {
    private Long id;
    private String sizeName;
    private int stock;
    private BigDecimal price;
    private BigDecimal priceAfterDiscount;
}
