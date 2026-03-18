package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.response.ProductVariantResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductVariantRequest;
import org.bbqqvv.backendecommerce.entity.ProductVariant;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SizeProductMapper.class}, builder = @Builder(disableBuilder = true))
public interface VariantMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    ProductVariant toProductVariant(ProductVariantRequest request);
    @Mapping(target = "sizeProducts", source = "sizeName")
    List<ProductVariant> toProductVariants(List<ProductVariantRequest> requests);
    @Mapping(target = "sizes",source = "productVariantSizes")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);
}
