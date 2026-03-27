package org.bbqqvv.backendecommerce.mapper;


import org.bbqqvv.backendecommerce.dto.request.SupportItemRequest;
import org.bbqqvv.backendecommerce.dto.response.SupportItemResponse;
import org.bbqqvv.backendecommerce.entity.SupportItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupportItemMapper {
    @Mapping(target = "img", ignore = true) // Bỏ qua ảnh vì sẽ xử lý riêng
    @Mapping(target = "id", ignore = true)
    SupportItem toEntity(SupportItemRequest supportItemRequest);
    SupportItemResponse toResponse(SupportItem supportItem);
}
