package org.bbqqvv.backendecommerce.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPostResponse {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String author;
    private String date;
    private String imageUrl;
    private String category; // Name of the category
    private String categorySlug;
    private List<String> tags;
    private String readingTime;
    private List<String> gallery;
    private List<Long> relatedProducts; // Only IDs as requested
}
