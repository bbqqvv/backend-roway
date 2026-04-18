package org.bbqqvv.backendecommerce.entity;

/**
 * Base interface for all product-related images.
 */
public interface ProductImage {
    String getImageUrl();
    void setImageUrl(String imageUrl);
    String getPublicId();
    void setPublicId(String publicId);
}
