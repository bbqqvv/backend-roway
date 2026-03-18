package org.bbqqvv.backendecommerce.mapper;
import org.bbqqvv.backendecommerce.dto.response.CartResponse;
import org.bbqqvv.backendecommerce.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring", uses = CartItemMapper.class, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CartMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "cartItems", target = "cartItems")
    CartResponse toCartResponse(Cart cart);
}
