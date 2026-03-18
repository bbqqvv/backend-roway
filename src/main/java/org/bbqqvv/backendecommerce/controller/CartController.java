package org.bbqqvv.backendecommerce.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.request.CartRequest;
import org.bbqqvv.backendecommerce.dto.response.CartResponse;
import org.bbqqvv.backendecommerce.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "Quản lý giỏ hàng")
public class CartController {
    private final CartService cartService;

    /**
     * Thêm hoặc cập nhật sản phẩm trong giỏ hàng
     */
    @PostMapping("/add-or-update")
    public ApiResponse<CartResponse> addOrUpdateProductInCart(@RequestBody @Valid CartRequest cartRequest) {
        return ApiResponse.<CartResponse>builder()
                .success(true)
                .data(cartService.addOrUpdateProductInCart(cartRequest))
                .message("Cart updated successfully")
                .build();
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng theo productId, size, color
     */
    @DeleteMapping("/remove")
    public ApiResponse<CartResponse> removeProductFromCart(
            @RequestParam Long productId,
            @RequestParam String sizeName,
            @RequestParam String color) {

        return ApiResponse.<CartResponse>builder()
                .success(true)
                .data(cartService.removeProductFromCart(productId, sizeName, color))
                .message("Product removed from cart")
                .build();
    }

    /**
     * Lấy giỏ hàng của người dùng hiện tại
     */
    @GetMapping
    public ApiResponse<CartResponse> getCartByUser() {
        return ApiResponse.<CartResponse>builder()
                .success(true)
                .data(cartService.getCartByUserId())
                .message("Cart retrieved successfully")
                .build();
    }
    /**
     * Tăng số lượng sản phẩm trong giỏ hàng
     */
    @PostMapping("/increase")
    public ApiResponse<CartResponse> increaseProductQuantity(@RequestBody @Valid CartRequest cartRequest) {
        return ApiResponse.<CartResponse>builder()
                .success(true)
                .data(cartService.increaseProductQuantity(cartRequest))
                .message("Product quantity increased")
                .build();
    }

    /**
     * Giảm số lượng sản phẩm trong giỏ hàng
     */
    @PostMapping("/decrease")
    public ApiResponse<CartResponse> decreaseProductQuantity(@RequestBody @Valid CartRequest cartRequest) {
        return ApiResponse.<CartResponse>builder()
                .success(true)
                .data(cartService.decreaseProductQuantity(cartRequest))
                .message("Product quantity decreased")
                .build();
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear")
    public ApiResponse<String> clearCart() {
        cartService.clearCart();
        return ApiResponse.<String>builder()
                .success(true)
                .data("Cart cleared successfully")
                .message("Cart has been emptied")
                .build();
    }
}
