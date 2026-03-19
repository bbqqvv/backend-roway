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
        return ApiResponse.success(cartService.addOrUpdateProductInCart(cartRequest), "Cart updated successfully");
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng theo productId, size, color
     */
    @DeleteMapping("/remove")
    public ApiResponse<CartResponse> removeProductFromCart(
            @RequestParam Long productId,
            @RequestParam String sizeName,
            @RequestParam String color) {

        return ApiResponse.success(cartService.removeProductFromCart(productId, sizeName, color), "Product removed from cart");
    }

    /**
     * Lấy giỏ hàng của người dùng hiện tại
     */
    @GetMapping
    public ApiResponse<CartResponse> getCartByUser() {
        return ApiResponse.success(cartService.getCartByUserId(), "Cart retrieved successfully");
    }
    /**
     * Tăng số lượng sản phẩm trong giỏ hàng
     */
    @PostMapping("/increase")
    public ApiResponse<CartResponse> increaseProductQuantity(@RequestBody @Valid CartRequest cartRequest) {
        return ApiResponse.success(cartService.increaseProductQuantity(cartRequest), "Product quantity increased");
    }

    /**
     * Giảm số lượng sản phẩm trong giỏ hàng
     */
    @PostMapping("/decrease")
    public ApiResponse<CartResponse> decreaseProductQuantity(@RequestBody @Valid CartRequest cartRequest) {
        return ApiResponse.success(cartService.decreaseProductQuantity(cartRequest), "Product quantity decreased");
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear")
    public ApiResponse<String> clearCart() {
        cartService.clearCart();
        return ApiResponse.success("Cart cleared successfully", "Cart has been emptied");
    }
}
