package org.bbqqvv.backendecommerce.exception.codes;

import lombok.Getter;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum SocialMarketingErrorCode implements ErrorCode {
    DISCOUNT_NOT_FOUND(5001, "Mã giảm giá không tồn tại", HttpStatus.NOT_FOUND),
    DISCOUNT_EXPIRED(5002, "Mã giảm giá đã hết hạn", HttpStatus.BAD_REQUEST),
    DISCOUNT_LIMIT_REACHED(5003, "Mã giảm giá đã hết lượt sử dụng", HttpStatus.BAD_REQUEST),
    DISCOUNT_NOT_APPLICABLE(5004, "Mã giảm giá không được áp dụng cho đơn hàng này", HttpStatus.BAD_REQUEST),
    DUPLICATE_DISCOUNT_CODE(5005, "Mã giảm giá đã tồn tại", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_FOUND(6001, "Đánh giá không tồn tại", HttpStatus.NOT_FOUND),
    REVIEW_ALREADY_EXISTS(6002, "Bạn đã đánh giá sản phẩm này rồi", HttpStatus.CONFLICT),
    REVIEW_EDIT_EXPIRED(6003, "Đã hết thời gian chỉnh sửa đánh giá", HttpStatus.BAD_REQUEST),
    FAVOURITE_ALREADY_EXISTS(6004, "Sản phẩm đã nằm trong danh sách yêu thích", HttpStatus.CONFLICT),
    FAVOURITE_NOT_FOUND(6005, "Không tìm thấy sản phẩm trong danh sách yêu thích", HttpStatus.NOT_FOUND),
    
    // Additional Discount Errors
    INVALID_DISCOUNT_CODE(5006, "Mã giảm giá không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_AMOUNT(5007, "Số tiền giảm giá không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_MAX_DISCOUNT_AMOUNT(5008, "Số tiền giảm tối đa không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_AMOUNT_LIMIT(5009, "Số tiền giảm vượt quá giới hạn", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_TYPE(5010, "Loại giảm giá không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_MIN_ORDER_VALUE(5011, "Giá trị đơn hàng tối thiểu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_USAGE_LIMIT(5012, "Giới hạn sử dụng không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DISCOUNT_DATES(5013, "Ngày bắt đầu hoặc kết thúc không hợp lệ", HttpStatus.BAD_REQUEST),
    DISCOUNT_ALREADY_EXPIRED(5014, "Mã giảm giá đã hết hạn", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_ACTIVE_DISCOUNT(5015, "Không thể xóa mã giảm giá đang hoạt động", HttpStatus.BAD_REQUEST),
    DISCOUNT_ALREADY_SAVED(5016, "Mã giảm giá này đã được lưu trước đó", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    @Override
    public HttpStatusCode getStatusCode() { return statusCode; }

    SocialMarketingErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

