package org.bbqqvv.backendecommerce.entity;

/**
 * Interface for images that support the staging (TEMP -> ACTIVE) flow.
 */
public interface StageableImage extends ProductImage {
    String getDraftId();
    void setDraftId(String draftId);
    
    ImageStatus getStatus();
    void setStatus(ImageStatus status);
    
    void setProduct(Product product);
}
