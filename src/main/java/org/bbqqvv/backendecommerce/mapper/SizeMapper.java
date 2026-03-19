package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.SizeCategoryRequest;
import org.bbqqvv.backendecommerce.dto.response.SizeCategoryResponse;
import org.bbqqvv.backendecommerce.entity.SizeCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SizeMapper {
    @Mapping(target = "category", ignore = true)
    SizeCategory toSize(SizeCategoryRequest sizeCategoryRequest);
    @Mapping(target = "categoryId",source = "category.id")
    SizeCategoryResponse toResponse(SizeCategory sizeCategory);

}
