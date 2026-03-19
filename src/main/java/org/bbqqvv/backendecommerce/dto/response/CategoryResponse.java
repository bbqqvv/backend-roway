package org.bbqqvv.backendecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String image;
    private String publicId;
    private List<SizeCategoryResponse> sizes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
