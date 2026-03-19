package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private java.math.BigDecimal price;

    // Support both 'tags' and 'tag' (for convenience in form-data)
    private Set<String> tags;

    public void setTag(String tag) {
        processTags(tag);
    }

    public void setTags(String tags) {
        processTags(tags);
    }

    private void processTags(String input) {
        if (input != null && !input.isBlank()) {
            if (this.tags == null) this.tags = new java.util.HashSet<>();
            java.util.Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.tags::add);
        }
    }

    private boolean sale;
    private boolean active;
    private boolean isOldProduct = false;
    private MultipartFile mainImage;
    private ImageMetadata mainImageMetadata;

    private List<MultipartFile> secondaryImages;
    private List<ImageMetadata> secondaryImageMetadata;

    private List<MultipartFile> descriptionImages;
    private List<ImageMetadata> descriptionImageMetadata;
    private List<ProductVariantRequest> variants;
}
