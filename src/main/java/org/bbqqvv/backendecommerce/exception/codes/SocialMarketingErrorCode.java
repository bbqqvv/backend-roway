package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum SocialMarketingErrorCode implements ErrorCode {
    FAVOURITE_NOT_FOUND(5001, "Sản phẩm yêu thích không tồn tại", HttpStatus.NOT_FOUND),
    FAVOURITE_ALREADY_EXISTS(5002, "Sản phẩm này đã có trong danh sách yêu thích", HttpStatus.CONFLICT),
    BANNER_NOT_FOUND(5010, "Banner not found", HttpStatus.NOT_FOUND),
    DISCOUNT_NOT_FOUND(5011, "Mã giảm giá không tồn tại", HttpStatus.NOT_FOUND),
    DISCOUNT_ALREADY_EXISTS(5012, "Mã giảm giá này đã tồn tại", HttpStatus.CONFLICT),
    INVALID_DISCOUNT_DATES(5013, "Ngày bắt đầu hoặc kết thúc không hợp lệ", HttpStatus.BAD_REQUEST),
    DISCOUNT_ALREADY_EXPIRED(5014, "Mã giảm giá đã hết hạn", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_ACTIVE_DISCOUNT(5015, "Không thể xóa mã giảm giá đang hoạt động", HttpStatus.BAD_REQUEST),
    DISCOUNT_ALREADY_SAVED(5016, "Mã giảm giá này đã được lưu trước đó", HttpStatus.BAD_REQUEST),
    DUPLICATE_DISCOUNT_CODE(5017, "Mã giảm giá đã tồn tại", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND(6001, "Đánh giá không tồn tại", HttpStatus.NOT_FOUND),
    REVIEW_EDIT_EXPIRED(6002, "Đã hết thời gian chỉnh sửa đánh giá", HttpStatus.BAD_REQUEST),
    SOCIAL_MARKETING_EXCEPTION(9000, "A social marketing error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    @Override public int getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatusCode getStatusCode() { return statusCode; }

    SocialMarketingErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
