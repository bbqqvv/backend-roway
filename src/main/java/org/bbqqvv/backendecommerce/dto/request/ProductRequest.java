package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must be less than 100 characters")
    private String name;
    @Size(max = 200, message = "Short description must be less than 200 characters")
    private String shortDescription;
    @Size(max = 8000, message = "Description must be less than 8000 characters")
    private String description;
    @NotBlank(message = "Product code is required")
    @Size(max = 100, message = "Product code must be less than 100 characters")
    private String productCode;
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    @Min(value = 0, message = "Sale percentage must be greater than or equal to 0")
    @Max(value = 100, message = "Sale percentage must be less than or equal to 100")
    private int salePercentage;
    private boolean featured;
    private Set<String> tags;
    private boolean sale;
    private boolean active ;
    private boolean isOldProduct = false;
    private MultipartFile mainImageUrl;
    private List<MultipartFile> secondaryImageUrls;
    private List<MultipartFile> descriptionImageUrls;
    private List<ProductVariantRequest> variants;
}
