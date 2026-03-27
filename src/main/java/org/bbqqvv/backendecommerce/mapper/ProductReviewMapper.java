package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.ProductReviewRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductReviewResponse;
import org.bbqqvv.backendecommerce.entity.ProductReview;
import org.bbqqvv.backendecommerce.entity.ProductReviewImage;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProductReviewMapper {

    // Chuyển DTO -> Entity (Dùng khi tạo mới ProductReview)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItem", ignore = true)
    @Mapping(target = "images", ignore = true)
    ProductReview toEntity(ProductReviewRequest request);

    // Chuyển Entity -> DTO (Dùng khi trả response)
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.username")
    @Mapping(target = "orderItemId", source = "orderItem.id")
    @Mapping(target = "imageUrls", source = "images", qualifiedByName = "allImageUrls")
    @Mapping(target = "helpfulCount", expression = "java(review.getLikedUserIds() != null ? review.getLikedUserIds().size() : 0)")
    ProductReviewResponse toResponse(ProductReview review);

    @Named("allImageUrls")
    default List<String> allImageUrls(List<ProductReviewImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream().map(ProductReviewImage::getImageUrl).toList();
    }
}
