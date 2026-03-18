package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.entity.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
}
