package org.bbqqvv.backendecommerce.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SizeCategoryResponse {
    private Long id;
    private String name;
    private Long categoryId;
}
