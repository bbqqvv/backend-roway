package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductReviewRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductReviewResponse;
import org.bbqqvv.backendecommerce.dto.response.ReviewStatsResponse;
import org.bbqqvv.backendecommerce.service.ProductReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products-review")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    /**
     * Thêm đánh giá mới hoặc cập nhật đánh giá sản phẩm
     */
    @PostMapping("/add-or-update")
    public ApiResponse<ProductReviewResponse> addOrUpdateReview(@ModelAttribute @Valid ProductReviewRequest reviewRequest) {
        return ApiResponse.success(productReviewService.addOrUpdateReview(reviewRequest), "Review added/updated successfully");
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<PageResponse<ProductReviewResponse>> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer rating,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PageResponse<ProductReviewResponse> response = productReviewService.getReviewsByProduct(productId, rating, pageable);
        return ApiResponse.success(response, "Reviews retrieved successfully");
    }

    /**
     * Thống kê đánh giá sản phẩm (trung bình, tổng, phân bố sao)
     */
    @GetMapping("/stats/{productId}")
    public ApiResponse<ReviewStatsResponse> getReviewStats(@PathVariable Long productId) {
        return ApiResponse.success(productReviewService.getReviewStats(productId), "Review stats retrieved successfully");
    }

    @GetMapping("/user")
    public ApiResponse<PageResponse<ProductReviewResponse>> getReviewsByUser(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PageResponse<ProductReviewResponse> response = productReviewService.getReviewsByUser(pageable);
        return ApiResponse.success(response, "User reviews retrieved successfully");
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductReviewResponse>> getAllReviews(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PageResponse<ProductReviewResponse> response = productReviewService.getAllReviews(pageable);
        return ApiResponse.success(response, "All reviews retrieved successfully");
    }

    @PostMapping("/{reviewId}/like")
    public ApiResponse<ProductReviewResponse> toggleLike(@PathVariable Long reviewId) {
        return ApiResponse.success(productReviewService.toggleLike(reviewId), "Review like toggled successfully");
    }

    /**
     * Xóa đánh giá sản phẩm
     */
    @DeleteMapping("/remove/{reviewId}")
    public ApiResponse<String> deleteReview(@PathVariable Long reviewId) {
        productReviewService.deleteReview(reviewId);
        return ApiResponse.success("Review deleted successfully", "Review removed");
    }
}
