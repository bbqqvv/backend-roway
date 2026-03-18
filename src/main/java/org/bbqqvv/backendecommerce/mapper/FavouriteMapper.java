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
    @Mapping(target = "nameProduct", source = "product.name")
    @Mapping(target = "productUrl", source = "product.slug")
    @Mapping(target = "stockStatus", ignore = true)
    @Mapping(target = "imageUrl", source = "product.mainImage.imageUrl")
    @Mapping(target = "price", expression = "java(calculatePrice(favourite))")
    FavouriteResponse toFavouriteResponse(Favourite favourite);

    default java.math.BigDecimal calculatePrice(Favourite favourite) {
        if (favourite.getProduct() == null ||
                favourite.getProduct().getVariants() == null ||
                favourite.getProduct().getVariants().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }

        var firstVariant = favourite.getProduct().getVariants().get(0);
        if (firstVariant.getProductVariantSizes() == null ||
                firstVariant.getProductVariantSizes().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }

        return firstVariant.getProductVariantSizes().get(0).getSizeProduct().getPrice();
    }
}
