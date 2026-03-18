package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.ProductReviewRequest;
import org.bbqqvv.backendecommerce.dto.response.ProductReviewResponse;
import org.bbqqvv.backendecommerce.entity.ProductReview;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
    ProductReviewResponse toResponse(ProductReview review);
}
