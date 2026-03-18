package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.response.CartItemResponse;
import org.bbqqvv.backendecommerce.entity.CartItem;
import org.bbqqvv.backendecommerce.entity.SizeProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CartItemMapper {
    @Mapping(target = "productId", source = "productVariant.product.id")
    @Mapping(target = "productName", source = "productVariant.product.name")
    @Mapping(target = "mainImageUrl", source = "productVariant.product.mainImage.imageUrl", defaultValue = "")
    @Mapping(target = "color", source = "productVariant.color")
    @Mapping(target = "stock", expression = "java(calculateStock(cartItem))")
    @Mapping(target = "inStock", expression = "java(calculateInStock(cartItem))")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    default int calculateStock(CartItem cartItem) {
        if (cartItem.getProductVariant() == null || cartItem.getProductVariant().getProductVariantSizes() == null) {
            return 0;
        }
        return cartItem.getProductVariant().getProductVariantSizes().stream()
                .filter(s -> s.getSizeProduct().getSizeName().equals(cartItem.getSizeName()))
                .findFirst()
                .map(SizeProductVariant::getStock)
                .orElse(0);
    }

    default boolean calculateInStock(CartItem cartItem) {
        return calculateStock(cartItem) > 0;
    }
}
