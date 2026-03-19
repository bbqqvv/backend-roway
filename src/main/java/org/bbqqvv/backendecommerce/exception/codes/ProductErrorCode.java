package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND(3001, "Sản phẩm không tồn tại", HttpStatus.NOT_FOUND),
    DUPLICATE_PRODUCT_CODE(3002, "Mã sản phẩm đã tồn tại", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(3003, "Danh mục không tồn tại", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND(3004, "Biến thể sản phẩm không tồn tại", HttpStatus.NOT_FOUND),
    SIZE_NOT_FOUND(3005, "Kích thước không tồn tại", HttpStatus.NOT_FOUND),
    OUT_OF_STOCK(3006, "Sản phẩm đã hết hàng", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_INVALID(3007, "Giá sản phẩm không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_OPTION(3008, "Tùy chọn sản phẩm không hợp lệ", HttpStatus.BAD_REQUEST),
    PRODUCT_VARIANT_NOT_FOUND(3009, "Biến thể sản phẩm không tìm thấy", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS(3010, "Danh mục đã tồn tại", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    @Override
    public HttpStatusCode getStatusCode() { return statusCode; }

    ProductErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

