package org.bbqqvv.backendecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API Response Wrapper")
public class ApiResponse<T> {

    @Schema(description = "Status code", example = "1000")
    @Builder.Default
    int code = 1000;

    @Schema(description = "Success flag", example = "true")
    boolean success;

    @Schema(description = "Message")
    String message;

    @Schema(description = "Response data")
    T data;

    @Schema(description = "Error details (e.g., validation errors)")
    Object details;
}
