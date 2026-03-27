package org.bbqqvv.backendecommerce.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewResponse {
    private Long id;
    private Long productId;
    private Long orderItemId;
    private String productName;
    private Long userId;
    private String userName;
    private int rating;
    private String reviewText;
    private List<String> imageUrls;
    private boolean isAnonymous;
    private LocalDateTime createdAt;
    private int helpfulCount;
    private boolean isLikedByCurrentUser;
}
