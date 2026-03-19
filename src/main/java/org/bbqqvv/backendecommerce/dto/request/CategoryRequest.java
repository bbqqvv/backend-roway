package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryRequest {
    @NotNull(message = "Name cannot be null")
    private String name;
    @NotNull(message = "Slug cannot be null")
    private String slug;
    private List<SizeCategoryRequest> sizes;
    private MultipartFile image;
    private ImageMetadata imageMetadata;
}
