package org.bbqqvv.backendecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private String productCode;
    private boolean featured;
    private boolean sale;
    private boolean active;
    private int reviewCount;
    private int salePercentage;
    private Set<String> tags;
    private String categoryName;
    private String mainImageUrl;
    private String mainImagePublicId;
    private List<String> secondaryImageUrls;
    private List<String> secondaryImagePublicIds;
    private List<String> descriptionImageUrls;
    private List<String> descriptionImagePublicIds;
    private List<ProductVariantResponse> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
