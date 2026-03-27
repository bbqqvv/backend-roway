package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.response.ImageMetadataResponse;
import org.bbqqvv.backendecommerce.dto.response.ProductVariantResponse;
import org.bbqqvv.backendecommerce.dto.request.ProductVariantRequest;
import org.bbqqvv.backendecommerce.entity.ProductVariant;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SizeProductMapper.class}, builder = @Builder(disableBuilder = true))
public interface VariantMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "productVariantSizes", ignore = true)
    ProductVariant toProductVariant(ProductVariantRequest request);

    @Mapping(target = "sizes", source = "productVariantSizes")
    @Mapping(target = "imageMetadata", source = "productVariant", qualifiedByName = "mapVariantImageMetadata")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    @Named("mapVariantImageMetadata")
    default ImageMetadataResponse mapVariantImageMetadata(ProductVariant variant) {
        if (variant.getImageUrl() == null) return null;
        return new ImageMetadataResponse(variant.getImageUrl(), variant.getPublicId());
    }
}
