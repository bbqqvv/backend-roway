package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageRegisterRequest {
    @NotBlank
    private String draftId;

    @NotBlank
    private String url;

    @NotBlank
    private String publicId;

    @NotBlank
    private String type; // MAIN, SECONDARY, VARIANT
}
