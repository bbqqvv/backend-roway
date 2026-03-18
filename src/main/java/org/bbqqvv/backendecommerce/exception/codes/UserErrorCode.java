package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(2001, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    USER_EXISTED(2002, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_USER_CREDENTIALS(2003, "Tên đăng nhập hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(2004, "Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED(2005, "Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN),
    PASSWORDS_DO_NOT_MATCH(2006, "Mật khẩu không khớp", HttpStatus.BAD_REQUEST),
    INVALID_OLD_PASSWORD(2007, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    @Override
    public HttpStatusCode getStatusCode() { return statusCode; }

    UserErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

