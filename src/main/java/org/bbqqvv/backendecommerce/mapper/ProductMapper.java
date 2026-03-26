package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.ProductRequest;
import org.bbqqvv.backendecommerce.dto.response.ImageMetadataResponse;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {VariantMapper.class}, builder = @Builder(disableBuilder = true))
public interface ProductMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mainImage", ignore = true)
    @Mapping(target = "secondaryImages", ignore = true)
    @Mapping(target = "descriptionImages", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Product toProduct(ProductRequest productRequest);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "mainImage.imageUrl", target = "mainImageUrl")
    @Mapping(source = "mainImage.publicId", target = "mainImagePublicId")
    @Mapping(target = "mainImageMetadata", source = "mainImage", qualifiedByName = "mapMainImageMetadata")
    @Mapping(source = "secondaryImages", target = "secondaryImageUrls", qualifiedByName = "mapSecondaryImageUrls")
    @Mapping(source = "secondaryImages", target = "secondaryImagePublicIds", qualifiedByName = "mapSecondaryImagePublicIds")
    @Mapping(target = "secondaryImages", source = "secondaryImages", qualifiedByName = "mapSecondaryImageMetadata")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "mapTagsToTagNames")
    @Mapping(target = "reviewCount", expression = "java(product.getReviews() != null ? product.getReviews().size() : 0)")
    ProductResponse toProductResponse(Product product);

    @Named("mapMainImageMetadata")
    default ImageMetadataResponse mapMainImageMetadata(ProductMainImage img) {
        if (img == null || img.getImageUrl() == null) return null;
        return new ImageMetadataResponse(img.getImageUrl(), img.getPublicId());
    }

    @Named("mapSecondaryImageMetadata")
    default List<ImageMetadataResponse> mapSecondaryImageMetadata(List<ProductSecondaryImage> images) {
        return images == null ? null : images.stream()
                .map(img -> new ImageMetadataResponse(img.getImageUrl(), img.getPublicId()))
                .collect(Collectors.toList());
    }

    @Named("mapDescriptionImageMetadata")
    default List<ImageMetadataResponse> mapDescriptionImageMetadata(List<ProductDescriptionImage> images) {
        return images == null ? null : images.stream()
                .map(img -> new ImageMetadataResponse(img.getImageUrl(), img.getPublicId()))
                .collect(Collectors.toList());
    }

    @Named("mapSecondaryImageUrls")
    default List<String> mapSecondaryImageUrls(List<ProductSecondaryImage> images) {
        return mapImageUrls(images);
    }
    @Named("mapSecondaryImagePublicIds")
    default List<String> mapSecondaryImagePublicIds(List<ProductSecondaryImage> images) {
        return mapImagePublicIds(images);
    }
    @Named("mapDescriptionImageUrls")
    default List<String> mapDescriptionImageUrls(List<ProductDescriptionImage> images) {
        return mapImageUrls(images);
    }
    @Named("mapDescriptionImagePublicIds")
    default List<String> mapDescriptionImagePublicIds(List<ProductDescriptionImage> images) {
        return mapImagePublicIds(images);
    }
    default List<String> mapImageUrls(List<? extends ProductImage> images) {
        return images == null ? null : images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }
    default List<String> mapImagePublicIds(List<? extends ProductImage> images) {
        return images == null ? null : images.stream()
                .map(ProductImage::getPublicId)
                .collect(Collectors.toList());
    }
    @Named("mapTagsToTagNames")
    default Set<String> mapTagsToTagNames(Set<Tag> tags) {
        return tags == null ? null : tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }
}
