package org.bbqqvv.backendecommerce.exception.global;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.exception.codes.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    // Bắt tất cả các loại ngoại lệ không xác định
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handleAllExceptions(Exception exception) {
        log.error("Unexpected error occurred: ", exception);
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(CommonErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(CommonErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    // Bắt AppException, khi lỗi được định nghĩa rõ ràng trong ErrorCode
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(exception.getMessage())
                .success(false)
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // Bắt lỗi vi phạm ràng buộc database (duy nhất, khóa ngoại...)
    @ExceptionHandler(value = DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error("Database integrity violation: ", exception);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(CommonErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage("Database conflict: possible duplicate data or constraint violation.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
    }

    // Bắt lỗi commit transaction thất bại (thường do JPA Auditing hoặc Validation)
    @ExceptionHandler(value = TransactionSystemException.class)
    ResponseEntity<ApiResponse> handleTransactionSystemException(TransactionSystemException exception) {
        log.error("Transaction failed: ", exception);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(CommonErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage("Transaction failure. Check data constraints and auditing fields.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    // Bắt AccessDeniedException, khi người dùng không có quyền truy cập
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = CommonErrorCode.ACCESS_DENIED;
        ApiResponse apiResponse = buildApiResponse(errorCode);

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // Bắt MethodArgumentNotValidException, khi validation thất bại
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        String enumKey = exception.getBindingResult().getFieldError().getDefaultMessage();
        ErrorCode errorCode = CommonErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            errorCode = resolveErrorCode(enumKey);
            var constraintViolation = exception.getBindingResult().getAllErrors().get(0).unwrap(ConstraintViolation.class);
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
        } catch (Exception e) {
            log.warn("Could not map validation error key '{}' to ErrorCode: {}", enumKey, e.getMessage());
        }

        ApiResponse apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(Objects.nonNull(attributes)
                        ? mapAttributes(errorCode.getMessage(), attributes)
                        : errorCode.getMessage())
                .success(false)
                .details(exception.getBindingResult().getFieldErrors().stream()
                        .collect(Collectors.toMap(
                                org.springframework.validation.FieldError::getField,
                                fieldErr -> Objects.requireNonNullElse(fieldErr.getDefaultMessage(), "Invalid value"),
                                (existing, replacement) -> existing
                        )))
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    private ErrorCode resolveErrorCode(String key) {
        try { return CommonErrorCode.valueOf(key); } catch (Exception ignored) {}
        try { return UserErrorCode.valueOf(key); } catch (Exception ignored) {}
        try { return ProductErrorCode.valueOf(key); } catch (Exception ignored) {}
        try { return CartOrderErrorCode.valueOf(key); } catch (Exception ignored) {}
        try { return SocialMarketingErrorCode.valueOf(key); } catch (Exception ignored) {}
        try { return InfrastructureAddressErrorCode.valueOf(key); } catch (Exception ignored) {}
        return CommonErrorCode.INVALID_KEY;
    }

    // Xử lý riêng để xây dựng ApiResponse
    private ApiResponse buildApiResponse(ErrorCode errorCode) {
        return ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .success(false)
                .build();
    }

    // Thay thế giá trị tham số trong thông điệp với các thuộc tính
    private String mapAttributes(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}

