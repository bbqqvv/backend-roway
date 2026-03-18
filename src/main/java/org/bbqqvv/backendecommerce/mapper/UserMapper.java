package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.entity.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.bbqqvv.backendecommerce.entity.Role;
import java.util.Set;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @Mapping(target = "authorities", expression = "java(mapAuthorities(user.getAuthorities()))")
    UserResponse toUserResponse(User user);

    default org.bbqqvv.backendecommerce.dto.response.RoleResponse mapAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return org.bbqqvv.backendecommerce.dto.response.RoleResponse.builder().role("ROLE_USER").build();
        }
        // Lấy role đầu tiên (thường là cái quan trọng nhất như ADMIN)
        Role firstRole = roles.stream()
                .sorted((r1, r2) -> r2.name().compareTo(r1.name())) // Admin > User
                .findFirst()
                .orElse(Role.ROLE_USER);
        
        return org.bbqqvv.backendecommerce.dto.response.RoleResponse.builder()
                .role(firstRole.name())
                .build();
    }
}
