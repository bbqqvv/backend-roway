package org.bbqqvv.backendecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {
    private MultipartFile image;
    private ImageMetadata imageMetadata;
    private List<SizeProductRequest> sizes;
    private String color;
    private String hexCode;
    private java.math.BigDecimal price;
    private Integer stock;

    public void setQuantity(Integer quantity) {
        this.stock = quantity;
    }
}
