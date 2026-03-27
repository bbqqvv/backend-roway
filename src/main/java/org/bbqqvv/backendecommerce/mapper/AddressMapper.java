package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.AddressRequest;
import org.bbqqvv.backendecommerce.dto.response.AddressResponse;
import org.bbqqvv.backendecommerce.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AddressMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "defaultAddress", source = "defaultAddress") // ✅ Map từ defaultAddress -> isDefault
    Address toAddress(AddressRequest addressRequest);
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "defaultAddress", source = "defaultAddress") // ✅ Map từ isDefault -> defaultAddress
    AddressResponse toAddressResponse(Address address);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(AddressRequest addressRequest, @MappingTarget Address existingAddress);
}
