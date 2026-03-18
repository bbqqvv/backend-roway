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
public class ProductReviewRequest {
    private Long productId;
    private int rating;
    private String reviewText;
    private List<MultipartFile> imageFiles;
}
