package org.bbqqvv.backendecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private List<SizeProductResponse> sizes;
    private String color;
    private String imageUrl;
    private String publicId;
}
