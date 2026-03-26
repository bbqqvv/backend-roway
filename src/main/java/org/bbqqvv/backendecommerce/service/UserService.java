package org.bbqqvv.backendecommerce.service;

import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ChangePasswordRequest;
import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.request.UserUpdateRequest;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.dto.response.UserUpdateResponse;
import org.bbqqvv.backendecommerce.entity.User;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);

    UserResponse getUserById(Long id);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(Long id, UserCreationRequest request);

    void deleteUser(Long id);

    UserResponse updateRole(Long id, org.bbqqvv.backendecommerce.dto.request.RoleUpdateRequest request);

    UserResponse updatePermissions(Long id, org.bbqqvv.backendecommerce.dto.request.PermissionsUpdateRequest request);

    User getUserByUsernameEntity(String username);

    boolean existsByUsername(String username);
//
    UserResponse updateUserInfo(UserUpdateRequest request);

    void changePassword(ChangePasswordRequest request);

    UserResponse getCurrentUser();
}
