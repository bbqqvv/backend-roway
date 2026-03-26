package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.entity.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.bbqqvv.backendecommerce.entity.Role;
import org.bbqqvv.backendecommerce.entity.Permission;
import java.util.Set;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @Mapping(target = "authorities", expression = "java(mapAuthorities(user.getAuthorities()))")
    @Mapping(target = "permissions", expression = "java(mapPermissions(user.getPermissions()))")
    UserResponse toUserResponse(User user);

    default Set<String> mapPermissions(Set<Permission> permissions) {
        if (permissions == null) return new java.util.HashSet<>();
        return permissions.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
    }

    default org.bbqqvv.backendecommerce.dto.response.RoleResponse mapAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return org.bbqqvv.backendecommerce.dto.response.RoleResponse.builder().role("ROLE_USER").build();
        }
        Role firstRole;
        if (roles.contains(Role.ROLE_ADMIN)) {
            firstRole = Role.ROLE_ADMIN;
        } else if (roles.contains(Role.ROLE_STAFF)) {
            firstRole = Role.ROLE_STAFF;
        } else {
            firstRole = Role.ROLE_USER;
        }
        
        return org.bbqqvv.backendecommerce.dto.response.RoleResponse.builder()
                .role(firstRole.name().replace("ROLE_", ""))
                .build();
    }
}
