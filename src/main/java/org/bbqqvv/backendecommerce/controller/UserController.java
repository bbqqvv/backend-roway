package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ChangePasswordRequest;
import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.request.UserUpdateRequest;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.dto.response.UserUpdateResponse;
import org.bbqqvv.backendecommerce.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Quản lý thông tin người dùng")
public class UserController {

    private final UserService userService;

    // Tạo người dùng mới
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return ApiResponse.success(userResponse, "User created successfully");
    }

    @PatchMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success("Password updated", "Password changed successfully");
    }

    // Lấy người dùng theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ApiResponse.success(userResponse, "User retrieved successfully");
    }

    // Lấy tất cả người dùng
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<UserResponse>> getAllUsers(Pageable pageable) {
        PageResponse<UserResponse> userResponses = userService.getAllUsers(pageable);
        return ApiResponse.success(userResponses, "User list retrieved successfully");
    }

    // Cập nhật thông tin người dùng
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @RequestBody @Valid UserCreationRequest request) {
        UserResponse userResponse = userService.updateUser(id, request);
        return ApiResponse.success(userResponse, "User updated successfully");
    }

    @PatchMapping("/me/update-info")
    public ApiResponse<UserUpdateResponse> updateUserInfo(@RequestBody @Valid UserUpdateRequest request) {
        UserUpdateResponse updatedUser = userService.updateUserInfo(request);
        return ApiResponse.success(updatedUser, "User info updated successfully");
    }

    // Xóa người dùng
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("User deleted", "User has been deleted successfully");
    }

    // Lấy thông tin user hiện tại từ token
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        UserResponse userResponse = userService.getCurrentUser();
        return ApiResponse.success(userResponse, "User retrieved successfully");
    }
}
