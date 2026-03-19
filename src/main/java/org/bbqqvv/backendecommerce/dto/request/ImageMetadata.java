package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata for uploaded images (from Cloudinary).
 * Used for "Direct Client Upload" flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageMetadata {
    @NotBlank(message = "Image URL is required")
    private String url;
    
    @NotBlank(message = "Public ID is required")
    private String publicId;
}
