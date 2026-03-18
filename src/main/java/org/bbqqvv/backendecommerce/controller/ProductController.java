package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.service.ProductService;
import org.bbqqvv.backendecommerce.service.RecentlyViewedProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Quản lý sản phẩm và lượt xem")
public class ProductController {

    private final ProductService productService;
    private final RecentlyViewedProductService recentlyViewedProductService;

    // Tạo mới một sản phẩm
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> createProduct(@ModelAttribute @Valid ProductRequest productRequest) {
        ProductResponse product = productService.createProduct(productRequest);
        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product created successfully")
                .data(product)
                .build();
    }

    // Lấy danh sách tất cả sản phẩm với phân trang
    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm", description = "Lấy toàn bộ danh sách sản phẩm có hỗ trợ phân trang (mặc định 10 sản phẩm/trang).")
    public ApiResponse<PageResponse<ProductResponse>> getAllProducts(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        PageResponse<ProductResponse> productPage = productService.getAllProducts(pageable);
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .success(true)
                .message("Product list retrieved successfully")
                .data(productPage)
                .build();
    }
    // Lấy sản phẩm nổi bật (featured) với phân trang
    @GetMapping("/featured")
    public ApiResponse<PageResponse<ProductResponse>> getFeaturedProducts(
            @PageableDefault(size = 8) Pageable pageable) {
        PageResponse<ProductResponse> featuredProducts = productService.getFeaturedProducts(pageable);
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .success(true)
                .message("Featured products retrieved successfully")
                .data(featuredProducts)
                .build();
    }

    // Lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product retrieved successfully")
                .data(product)
                .build();
    }

    // Lấy sản phẩm theo Slug
    @GetMapping("slug/{slug}")
    public ApiResponse<ProductResponse> getProductBySlug(@PathVariable String slug) {
        ProductResponse product = productService.getProductBySlug(slug);
        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product retrieved successfully")
                .data(product)
                .build();
    }



    // Lấy sản phẩm theo danh mục với phân trang
    @GetMapping("/find-by-category-slug/{slug}")
    public ApiResponse<PageResponse<ProductResponse>> findProductByCategorySlug(
            @PathVariable String slug,
            @PageableDefault(size = 9) Pageable pageable) {
        PageResponse<ProductResponse> productPage = productService.findProductByCategorySlug(slug, pageable);
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .success(true)
                .message("Products retrieved by category successfully")
                .data(productPage)
                .build();
    }

    // Cập nhật thông tin sản phẩm
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @ModelAttribute @Valid ProductRequest productRequest) {
        ProductResponse updatedProduct = productService.updateProduct(id, productRequest);
        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product updated successfully")
                .data(updatedProduct)
                .build();
    }

    // Xóa sản phẩm
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return ApiResponse.<String>builder()
                .success(true)
                .message(deleted ? "Product deleted successfully" : "Product not found")
                .data(deleted ? "Deleted" : "Not found")
                .build();
    }

    // Tìm kiếm sản phẩm theo tên
    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductResponse>> searchProductsByName(
            @RequestParam String name,
            @PageableDefault(page = 0, size = 9) Pageable pageable) {
        PageResponse<ProductResponse> productPage = productService.searchProductsByName(name, pageable);
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .success(true)
                .message("Products retrieved successfully")
                .data(productPage)
                .build();
    }

    /**
     * Đánh dấu sản phẩm đã xem
     */
    @PostMapping("/{productId}/mark")
    public ApiResponse<String> markProductAsViewed(@PathVariable Long productId) {
        recentlyViewedProductService.markProductAsViewed(productId);
        return ApiResponse.<String>builder()
                .success(true)
                .data("Product marked as viewed")
                .message("Marked successfully")
                .build();
    }

    /**
     * Lấy danh sách sản phẩm đã xem gần đây
     */
    @GetMapping("/recently-viewed")
    public ApiResponse<PageResponse<ProductResponse>> getRecentlyViewedProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<ProductResponse> response = recentlyViewedProductService.getRecentlyViewedProducts(pageable);
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .success(true)
                .data(response)
                .message("Fetched successfully")
                .build();
    }
    @PostMapping("/viewed-sync")
    public ApiResponse<String> syncViewedProducts(@RequestBody List<Long> productIds) {
        recentlyViewedProductService.syncViewedProducts(productIds);
        return ApiResponse.<String>builder()
                .success(true)
                .data("Synced successfully")
                .message("Local viewed products synced")
                .build();
    }
}
