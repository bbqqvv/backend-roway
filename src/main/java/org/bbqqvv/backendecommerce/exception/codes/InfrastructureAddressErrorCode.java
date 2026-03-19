package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum InfrastructureAddressErrorCode implements ErrorCode {
    ADDRESS_NOT_FOUND(8001, "Địa chỉ không tồn tại", HttpStatus.NOT_FOUND),
    ADDRESS_LIMIT_REACHED(8002, "Số lượng địa chỉ tối đa là 5", HttpStatus.BAD_REQUEST),
    INVALID_CITY(8003, "Thành phố không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DISTRICT(8004, "Quận/Huyện không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_WARD(8005, "Phường/Xã không hợp lệ", HttpStatus.BAD_REQUEST),
    DEFAULT_ADDRESS_REQUIRED(8006, "Phải có ít nhất một địa chỉ mặc định", HttpStatus.BAD_REQUEST),
    ADDRESS_DEFAULT_CANNOT_DELETE(8007, "Không thể xóa địa chỉ mặc định", HttpStatus.BAD_REQUEST),
    IMAGE_UPLOAD_FAILED(8008, "Tải ảnh lên thất bại", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    @Override public int getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatusCode getStatusCode() { return statusCode; }

    InfrastructureAddressErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = (HttpStatus) statusCode;
    }
}
