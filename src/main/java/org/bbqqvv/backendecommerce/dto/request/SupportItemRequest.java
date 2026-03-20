package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportItemRequest {
    @NotNull(message = "Hình ảnh không được để trống")
    private MultipartFile img;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Giờ làm việc không được để trống")
    private String hours;

    @NotBlank(message = "Thông tin liên hệ không được để trống")
    private String contact;

    @NotBlank(message = "Đường dẫn liên hệ không được để trống")
    private String link;

    @NotBlank(message = "Màu nền không được để trống")
    private String bgColor;
}
