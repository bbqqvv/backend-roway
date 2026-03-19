package org.bbqqvv.backendecommerce.service;

import org.bbqqvv.backendecommerce.dto.request.CartRequest;
import org.bbqqvv.backendecommerce.dto.response.CartResponse;

import java.math.BigDecimal;

public interface CartService {

    /**
     * Thêm hoặc cập nhật sản phẩm trong giỏ hàng
     */
    CartResponse addOrUpdateProductInCart(CartRequest cartRequest);

    /**
     * Xóa một sản phẩm khỏi giỏ hàng theo productId, size, color
     */
    CartResponse removeProductFromCart(Long productId, String sizeName, String color);

    /**
     * Lấy giỏ hàng của người dùng hiện tại
     */
    CartResponse getCartByUserId();
    /**
     * Tăng số lượng sản phẩm trong giỏ hàng
     */
    CartResponse increaseProductQuantity(CartRequest cartRequest);

    /**
     * Giảm số lượng sản phẩm trong giỏ hàng
     */
    CartResponse decreaseProductQuantity(CartRequest cartRequest);

    /**
     * Xóa toàn bộ giỏ hàng
     */
    void clearCart();

    BigDecimal getTotalCartAmount();
}
