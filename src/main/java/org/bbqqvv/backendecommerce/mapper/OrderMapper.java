package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.response.OrderResponse;
import org.bbqqvv.backendecommerce.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {
    @Mapping(target = "name", source = "recipientName")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderItems", source = "orderItems")
    @Mapping(target = "address", source = "fullAddress")
    @Mapping(target = "discountCode", source = "discount.code", defaultValue = "")
    @Mapping(target = "discountAmount", source = "discountAmount", defaultValue = "0")
    @Mapping(target = "paymentUrl", ignore = true)
    OrderResponse toOrderResponse(Order order);
}
