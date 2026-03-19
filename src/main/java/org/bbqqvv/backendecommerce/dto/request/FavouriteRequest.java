package org.bbqqvv.backendecommerce.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class FavouriteRequest {
    private Long productId;
    private Long sizeProductVariantId;
}
