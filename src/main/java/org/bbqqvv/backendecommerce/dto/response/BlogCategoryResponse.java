package org.bbqqvv.backendecommerce.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCategoryResponse {
    private Long id;
    private String name;
    private String slug;
}
