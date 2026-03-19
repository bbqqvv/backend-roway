package org.bbqqvv.backendecommerce.dto.response;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class FavouriteResponse {
    private Long id;
    private Long userId;
    private String nameProduct;
    private String imageUrl;
    private String productUrl;
    private String stockStatus;
    private BigDecimal price;
    private String color;
    private String size;
    private Long sizeProductVariantId;
}
