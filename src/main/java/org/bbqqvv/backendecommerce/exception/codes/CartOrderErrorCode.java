package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum CartOrderErrorCode implements ErrorCode {
    CART_NOT_FOUND(4001, "Giỏ hàng không tồn tại", HttpStatus.NOT_FOUND),
    ITEM_NOT_FOUND_IN_CART(4002, "Sản phẩm không có trong giỏ hàng", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(4002, "Sản phẩm không có trong giỏ hàng", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(4003, "Đơn hàng không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS(4004, "Trạng thái đơn hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    QUANTITY_EXCEEDED(4005, "Số lượng sản phẩm vượt quá giới hạn", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK_ORDER(4006, "Sản phẩm đã hết hàng", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_PAID(4007, "Đơn hàng đã được thanh toán trước đó", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(4008, "Thanh toán thất bại", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_REQUIRED(4009, "Địa chỉ nhận hàng là bắt buộc", HttpStatus.BAD_REQUEST),
    ORDER_CODE_ALREADY_EXISTS(4010, "Mã đơn hàng đã tồn tại", HttpStatus.CONFLICT),
    EMPTY_CART(4011, "Giỏ hàng trống", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_ORDER(4012, "Không thể hủy đơn hàng", HttpStatus.BAD_REQUEST),
    ORDER_NOT_COMPLETED(4013, "Đơn hàng chưa hoàn thành", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    @Override public int getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatusCode getStatusCode() { return statusCode; }

    CartOrderErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = (HttpStatus) statusCode;
    }
}
