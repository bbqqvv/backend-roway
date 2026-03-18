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
    private MultipartFile imageUrl;
    private List<SizeProductRequest> sizes;
    private String color;
}
