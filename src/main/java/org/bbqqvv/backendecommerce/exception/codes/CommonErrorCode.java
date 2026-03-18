package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum CommonErrorCode implements ErrorCode {
    UNCATEGORIZED_EXCEPTION(1000, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Key không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1002, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1003, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    INVALID_REQUEST(1004, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(7004, "Tài nguyên không tồn tại", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    @Override
    public HttpStatusCode getStatusCode() { return statusCode; }

    CommonErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
