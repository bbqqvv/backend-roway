package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.FavouriteRequest;
import org.bbqqvv.backendecommerce.dto.response.FavouriteResponse;
import org.bbqqvv.backendecommerce.entity.Favourite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface FavouriteMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "product.id", source = "productId")
    Favourite toFavourite(FavouriteRequest favouriteRequest);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "nameProduct", source = "product.name")
    @Mapping(target = "productUrl", source = "product.slug")
    @Mapping(target = "stockStatus", ignore = true)
    @Mapping(target = "imageUrl", expression = "java(resolveImageUrl(favourite))")
    @Mapping(target = "price", expression = "java(calculatePrice(favourite))")
    @Mapping(target = "color", source = "sizeProductVariant.productVariant.color")
    @Mapping(target = "size", source = "sizeProductVariant.sizeProduct.sizeName")
    @Mapping(target = "sizeProductVariantId", source = "sizeProductVariant.id")
    FavouriteResponse toFavouriteResponse(Favourite favourite);

    default String resolveImageUrl(Favourite favourite) {
        if (favourite.getSizeProductVariant() != null && 
            favourite.getSizeProductVariant().getProductVariant() != null &&
            favourite.getSizeProductVariant().getProductVariant().getImageUrl() != null) {
            return favourite.getSizeProductVariant().getProductVariant().getImageUrl();
        }
        return favourite.getProduct() != null && favourite.getProduct().getMainImage() != null ? 
               favourite.getProduct().getMainImage().getImageUrl() : null;
    }

    default java.math.BigDecimal calculatePrice(Favourite favourite) {
        if (favourite.getSizeProductVariant() != null) {
            var spv = favourite.getSizeProductVariant();
            return spv.getPriceAfterDiscount() != null ? spv.getPriceAfterDiscount() : spv.getPrice();
        }

        if (favourite.getProduct() == null ||
                favourite.getProduct().getVariants() == null ||
                favourite.getProduct().getVariants().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }

        // Tìm giá thấp nhất từ các biến thể và kích thước
        return favourite.getProduct().getVariants().stream()
                .filter(v -> v.getProductVariantSizes() != null)
                .flatMap(v -> v.getProductVariantSizes().stream())
                .map(spv -> spv.getPriceAfterDiscount() != null ? spv.getPriceAfterDiscount() : spv.getPrice())
                .filter(java.util.Objects::nonNull)
                .min(java.math.BigDecimal::compareTo)
                .orElse(java.math.BigDecimal.ZERO);
    }
}
