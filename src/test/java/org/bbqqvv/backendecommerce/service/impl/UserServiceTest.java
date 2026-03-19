package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.request.ChangePasswordRequest;
import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.UserErrorCode;
import org.bbqqvv.backendecommerce.mapper.UserMapper;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserCreationRequest userCreationRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .email("test@example.com")
                .build();

        userCreationRequest = new UserCreationRequest();
        userCreationRequest.setUsername("testuser");
        userCreationRequest.setPassword("password123");
        userCreationRequest.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Tạo người dùng mới thành công")
    void createUser_shouldReturnUserResponse_whenValidRequest() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userMapper.toUser(any())).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        // Act
        UserResponse response = userService.createUser(userCreationRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Tạo người dùng mới thất bại - Username đã tồn tại")
    void createUser_shouldThrowException_whenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(userCreationRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_EXISTED);
    }

    @Test
    @DisplayName("Đổi mật khẩu thành công")
    void changePassword_shouldSucceed_whenValidRequest() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("old_password", "encoded_password")).thenReturn(true);
            when(passwordEncoder.encode("new_password")).thenReturn("new_encoded_password");

            ChangePasswordRequest request = new ChangePasswordRequest("old_password", "new_password", "new_password");

            // Act
            userService.changePassword(request);

            // Assert
            verify(userRepository).save(user);
            assertThat(user.getPassword()).isEqualTo("new_encoded_password");
        }
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại - Sai mật khẩu cũ")
    void changePassword_shouldThrowException_whenOldPasswordInvalid() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

            ChangePasswordRequest request = new ChangePasswordRequest("wrong_password", "new", "new");

            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_OLD_PASSWORD);
        }
    }
}
