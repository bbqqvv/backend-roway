package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.response.OrderItemResponse;
import org.bbqqvv.backendecommerce.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ProductMapper.class, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface OrderItemMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "mainImageUrl", source = "product.mainImage.imageUrl", defaultValue = "")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}


