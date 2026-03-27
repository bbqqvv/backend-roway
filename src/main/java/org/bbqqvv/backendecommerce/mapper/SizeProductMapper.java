package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.response.SizeProductResponse;
import org.bbqqvv.backendecommerce.entity.SizeProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface SizeProductMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "sizeName", source = "sizeProduct.sizeName")
    @Mapping(target = "stock", source = "stock")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "priceAfterDiscount", source = "priceAfterDiscount")
    SizeProductResponse toSizeProductVariantResponse(SizeProductVariant sizeProductVariant);
}
