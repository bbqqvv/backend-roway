package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(2001, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2002, "Người dùng đã tồn tại", HttpStatus.CONFLICT),
    USER_EXISTED(2002, "Người dùng đã tồn tại", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(2003, "Thông tin đăng nhập không chính xác", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(2004, "Tài khoản bị khóa", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(2005, "Không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    INVALID_OTP(2006, "Mã OTP không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(2007, "Email đã tồn tại", HttpStatus.CONFLICT),
    EMAIL_EXISTED(2007, "Email đã tồn tại", HttpStatus.CONFLICT),
    GOOGLE_ACCOUNT_EXISTED(2008, "Tài khoản Google đã tồn tại", HttpStatus.CONFLICT),
    INVALID_PASSWORD(2009, "Mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_OLD_PASSWORD(2010, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST),
    PASSWORDS_DO_NOT_MATCH(2011, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(2012, "Quyền người dùng không tồn tại", HttpStatus.NOT_FOUND),
    USER_ERROR(2999, "Lỗi người dùng không xác định", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    @Override public int getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatusCode getStatusCode() { return statusCode; }

    UserErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = (HttpStatus) statusCode;
    }
}
