package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductReviewRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductReviewResponse;
import org.bbqqvv.backendecommerce.service.ProductReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    /**
     * Thêm đánh giá mới hoặc cập nhật đánh giá sản phẩm
     */
    @PostMapping("/add-or-update")
    public ApiResponse<ProductReviewResponse> addOrUpdateReview(@ModelAttribute @Valid ProductReviewRequest reviewRequest) {
        return ApiResponse.<ProductReviewResponse>builder()
                .success(true)
                .data(productReviewService.addOrUpdateReview(reviewRequest))
                .message("Review added/updated successfully")
                .build();
    }
    @GetMapping("/product/{productId}")
    public ApiResponse<PageResponse<ProductReviewResponse>> getReviewsByProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PageResponse<ProductReviewResponse> response = productReviewService.getReviewsByProduct(productId, pageable);
        return ApiResponse.<PageResponse<ProductReviewResponse>>builder()
                .success(true)
                .message("Reviews retrieved successfully")
                .data(response)
                .build();
    }

    @GetMapping("/user")
    public ApiResponse<PageResponse<ProductReviewResponse>> getReviewsByUser(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PageResponse<ProductReviewResponse> response = productReviewService.getReviewsByUser(pageable);
        return ApiResponse.<PageResponse<ProductReviewResponse>>builder()
                .success(true)
                .message("User reviews retrieved successfully")
                .data(response)
                .build();
    }

    /**
     * Xóa đánh giá sản phẩm
     */
    @DeleteMapping("/remove/{reviewId}")
    public ApiResponse<String> deleteReview(@PathVariable Long reviewId) {
        productReviewService.deleteReview(reviewId);
        return ApiResponse.<String>builder()
                .success(true)
                .data("Review deleted successfully")
                .message("Review removed")
                .build();
    }
}
