package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.response.SizeProductResponse;
import org.bbqqvv.backendecommerce.entity.SizeProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface SizeProductMapper {
    @Mapping(target = "sizeName", source = "sizeProduct.sizeName")
    @Mapping(target = "price", source = "sizeProduct.price")
    @Mapping(target = "priceAfterDiscount", source = "sizeProduct.priceAfterDiscount")
    SizeProductResponse toSizeProductVariantResponse(SizeProductVariant sizeProductVariant);
}
