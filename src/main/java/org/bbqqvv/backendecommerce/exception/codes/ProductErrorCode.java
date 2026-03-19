package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND(1001, "Sản phẩm không tồn tại", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1002, "Danh mục không tồn tại", HttpStatus.NOT_FOUND),
    SIZE_NOT_FOUND(1003, "Kích thước không tồn tại", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(1004, "Số lượng trong kho không đủ", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND(1005, "Ảnh không tồn tại", HttpStatus.NOT_FOUND),
    DUPLICATE_PRODUCT_NAME(1006, "Tên sản phẩm đã tồn tại", HttpStatus.CONFLICT),
    PRODUCT_ALREADY_EXISTS(1007, "Sản phẩm này đã tồn tại", HttpStatus.CONFLICT),
    CATEGORY_ALREADY_EXISTS(1008, "Danh mục đã tồn tại", HttpStatus.CONFLICT),
    DUPLICATE_PRODUCT_CODE(1009, "Mã sản phẩm đã tồn tại", HttpStatus.CONFLICT),
    PRODUCT_VARIANT_NOT_FOUND(1010, "Biến thể sản phẩm không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_PRODUCT_OPTION(1011, "Tùy chọn sản phẩm không hợp lệ", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK(1012, "Sản phẩm đã hết hàng", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_DATA(1013, "Dữ liệu sản phẩm không hợp lệ", HttpStatus.BAD_REQUEST),
    PRODUCT_ERROR(1999, "Lỗi sản phẩm không xác định", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    @Override public int getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatusCode getStatusCode() { return statusCode; }

    ProductErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = (HttpStatus) statusCode;
    }
}
