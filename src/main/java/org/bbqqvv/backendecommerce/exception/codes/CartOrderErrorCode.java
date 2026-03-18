package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum CartOrderErrorCode implements ErrorCode {
    CART_NOT_FOUND(4001, "Giỏ hàng không tồn tại", HttpStatus.NOT_FOUND),
    EMPTY_CART(4002, "Giỏ hàng đang trống", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(4003, "Đơn hàng không tồn tại", HttpStatus.NOT_FOUND),
    CANNOT_CANCEL_ORDER(4004, "Không thể hủy đơn hàng này", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS(4005, "Trạng thái đơn hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_FUNDS(4006, "Số dư không đủ", HttpStatus.BAD_REQUEST),
    ORDER_NOT_COMPLETED(4007, "Đơn hàng chưa hoàn thành", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_NOT_FOUND(4008, "Sản phẩm trong đơn hàng không tồn tại", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(4009, "Sản phẩm trong giỏ hàng không tồn tại", HttpStatus.NOT_FOUND),
    CART_ITEM_ALREADY_EXISTS(4010, "Sản phẩm đã có trong giỏ hàng", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
    

    @Override
    public HttpStatusCode getStatusCode() { return statusCode; }

    CartOrderErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

