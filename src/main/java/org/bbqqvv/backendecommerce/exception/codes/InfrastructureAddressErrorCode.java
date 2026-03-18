package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum InfrastructureAddressErrorCode implements ErrorCode {
    IMAGE_UPLOAD_FAILED(7001, "Tải ảnh lên thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_FORMAT_INVALID(7002, "Định dạng ảnh không hợp lệ", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED(7003, "Gửi email thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    ADDRESS_NOT_FOUND(8001, "Địa chỉ không tồn tại", HttpStatus.NOT_FOUND),
    ADDRESS_DEFAULT_CANNOT_DELETE(8002, "Không thể xóa địa chỉ mặc định", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER(8003, "Số điện thoại không hợp lệ", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    @Override
    public HttpStatusCode getStatusCode() { return statusCode; }

    InfrastructureAddressErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

