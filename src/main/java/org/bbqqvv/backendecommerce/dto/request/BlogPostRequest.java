package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPostRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Slug không được để trống")
    private String slug;

    private String summary;

    private String content;

    private String author;

    private String date;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private String readingTime;

    // URL ảnh (Đã được upload lên Cloudinary từ Frontend)
    private String imageUrl;

    private List<String> tags;

    private List<String> gallery;

    private List<Long> relatedProductIds;
}
