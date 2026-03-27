package org.bbqqvv.backendecommerce.service;

import jakarta.validation.Valid;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductReviewRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductReviewResponse;
import org.bbqqvv.backendecommerce.dto.response.ReviewStatsResponse;
import org.springframework.data.domain.Pageable;

public interface ProductReviewService {
    ProductReviewResponse addOrUpdateReview(@Valid ProductReviewRequest reviewRequest);
    PageResponse<ProductReviewResponse> getReviewsByProduct(Long productId, Pageable pageable);
    PageResponse<ProductReviewResponse> getReviewsByProduct(Long productId, Integer rating, Pageable pageable);
    PageResponse<ProductReviewResponse> getReviewsByUser(Pageable pageable);
    ReviewStatsResponse getReviewStats(Long productId);
    PageResponse<ProductReviewResponse> getAllReviews(Pageable pageable);
    void deleteReview(Long reviewId);
    ProductReviewResponse toggleLike(Long reviewId);
}
