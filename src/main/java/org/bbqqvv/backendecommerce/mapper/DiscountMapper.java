package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.DiscountRequest;
import org.bbqqvv.backendecommerce.dto.response.DiscountResponse;
import org.bbqqvv.backendecommerce.entity.Discount;
import org.bbqqvv.backendecommerce.entity.DiscountProduct;
import org.bbqqvv.backendecommerce.entity.DiscountUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface DiscountMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "applicableUsers", target = "applicableUsers", ignore = true) // Chuyển sang service xử lý
    @Mapping(source = "applicableProducts", target = "applicableProducts", ignore = true) // Chuyển sang service xử lý
    Discount toDiscount(DiscountRequest discountRequest);

    @Mapping(source = "applicableUsers", target = "applicableUsersCount", qualifiedByName = "countList")
    @Mapping(source = "applicableProducts", target = "applicableProductsCount", qualifiedByName = "countList")
    DiscountResponse toDiscountResponse(Discount discount);

    @Named("countList")
    default int countList(List<?> list) {
        return list == null ? 0 : list.size();
    }

}
