package org.bbqqvv.backendecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API Response Wrapper")
public record ApiResponse<T>(
    @Schema(description = "Status code", example = "1000")
    int code,

    @Schema(description = "Success flag", example = "true")
    boolean success,

    @Schema(description = "Message")
    String message,

    @Schema(description = "Response data")
    T data,

    @Schema(description = "Error details (e.g., validation errors)")
    Object details
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(1000, true, "Success", data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(1000, true, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(9999, false, message, null, null);
    }
    
    // Static factory for error with details (e.g. validation errors)
    public static <T> ApiResponse<T> error(String message, Object details) {
        return new ApiResponse<>(9999, false, message, null, details);
    }
}
