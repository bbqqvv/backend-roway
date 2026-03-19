package org.bbqqvv.backendecommerce.service.auth;

import org.bbqqvv.backendecommerce.dto.request.AuthenticationRequest;
import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.entity.AuthProvider;
import org.bbqqvv.backendecommerce.entity.Role;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.mapper.UserMapper;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.config.jwt.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserCreationRequest registerRequest;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = UserCreationRequest.builder()
                .username("test_user")
                .password("password123")
                .email("test@example.com")
                .build();

        user = User.builder()
                .id(1L)
                .username("test_user")
                .email("test@example.com")
                .password("encoded_password")
                .provider(AuthProvider.LOCAL)
                .authorities(Set.of(Role.ROLE_USER))
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("test_user")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("Đăng ký thành công - Trả về UserResponse")
    void register_shouldReturnUserResponse_whenValidRequest() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.count()).thenReturn(1L); // Đã có user nên role sẽ là ROLE_USER
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        // Act
        UserResponse response = authenticationService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("test_user");
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng ký thất bại - Email đã tồn tại")
    void register_shouldThrowException_whenEmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email này đã được đăng ký");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng nhập thành công - Trả về JWT token")
    void login_shouldReturnToken_whenValidCredentials() {
        // Arrange
        AuthenticationRequest loginRequest = new AuthenticationRequest("test_user", "password123");
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(any(UserDetails.class))).thenReturn("mock_jwt_token");

        // Act
        String token = authenticationService.login(loginRequest);

        // Assert
        assertThat(token).isEqualTo("mock_jwt_token");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenUtil, times(1)).generateToken(any(UserDetails.class));
    }
}
