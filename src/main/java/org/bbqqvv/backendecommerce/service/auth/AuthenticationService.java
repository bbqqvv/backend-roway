package org.bbqqvv.backendecommerce.service.auth;

import org.bbqqvv.backendecommerce.config.jwt.JwtTokenUtil;
import org.bbqqvv.backendecommerce.dto.request.AuthenticationRequest;
import org.bbqqvv.backendecommerce.dto.request.RefreshTokenRequest;
import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.response.JwtResponse;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.entity.AuthProvider;
import org.bbqqvv.backendecommerce.entity.Role;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.entity.RefreshToken;
import org.bbqqvv.backendecommerce.mapper.UserMapper;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.UserErrorCode;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.service.auth.RefreshTokenService;
import org.bbqqvv.backendecommerce.util.ValidateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AuthenticationService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationService(UserMapper userMapper,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager,
                                 JwtTokenUtil jwtTokenUtil, 
                                 UserRepository userRepository,
                                 RefreshTokenService refreshTokenService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }
    public UserResponse register(UserCreationRequest registerUserDto) {
        ValidateUtils.validateUsername(registerUserDto.getUsername());

        // Kiểm tra xem email đã tồn tại chưa
        Optional<User> existingUser = userRepository.findByEmail(registerUserDto.getEmail());
        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // Nếu tài khoản đã đăng ký bằng Google, không cho phép đăng ký lại bằng email/password
            if (user.getProvider() == AuthProvider.GOOGLE) {
                throw new AppException(UserErrorCode.GOOGLE_ACCOUNT_EXISTED);
            }

            throw new AppException(UserErrorCode.EMAIL_EXISTED);
        }

        // Mã hóa mật khẩu trước khi lưu vào database
        String encodedPassword = passwordEncoder.encode(registerUserDto.getPassword());

        // Tạo user mới với provider mặc định là LOCAL (email/password)
        boolean isFirstUser = userRepository.count() == 0;
        logger.info("Is first user: {}", isFirstUser);

        User newUser = User.builder()
                .username(registerUserDto.getUsername())
                .password(encodedPassword)
                .email(registerUserDto.getEmail())
                .provider(AuthProvider.LOCAL)
                .authorities(Set.of(isFirstUser ? Role.ROLE_ADMIN : Role.ROLE_USER)) // ✅ Set role
                .build();
        logger.info("Created user: {}", newUser);

        // Lưu user vào database
        userRepository.save(newUser);

        return  userMapper.toUserResponse(newUser);
    }

    @Transactional
    public JwtResponse login(AuthenticationRequest loginUserDto) {
        try {
            logger.info("Service: Start login process for user: {}", loginUserDto.getUsername());
            
            logger.info("Service: Authenticating with AuthenticationManager...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUserDto.getUsername(), loginUserDto.getPassword())
            );
            logger.info("Service: Authentication successful for user: {}", loginUserDto.getUsername());
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.info("Service: Retrieving user from repository: {}", userDetails.getUsername());
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

            logger.info("Service: Generating JWT token...");
            String token = jwtTokenUtil.generateToken(userDetails);
            
            logger.info("Service: Deleting old refresh tokens for user ID: {}", user.getId());
            refreshTokenService.deleteByUser(user);
            
            logger.info("Service: Creating new refresh token...");
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            logger.info("Service: Login process completed successfully for user: {}", loginUserDto.getUsername());
            return JwtResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken.getToken())
                    .build();
        } catch (org.springframework.security.core.AuthenticationException e) {
            logger.error("Service: Authentication failed for user {}: {}", loginUserDto.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Service: Unexpected error during login for user {}: ", loginUserDto.getUsername(), e);
            throw e;
        }
    }

    @Transactional
    public JwtResponse refreshToken(org.bbqqvv.backendecommerce.dto.request.RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    User user = token.getUser();
                    
                    // Xoay vòng Token (Rotate): Tạo cặp Access/Refresh mới, hủy cái cũ
                    RefreshToken rotatedRefreshToken = refreshTokenService.rotateToken(token);
                    String accessToken = jwtTokenUtil.generateToken(toUserDetails(user));
                    
                    return JwtResponse.builder()
                            .token(accessToken)
                            .refreshToken(rotatedRefreshToken.getToken())
                            .build();
                })
                .orElseThrow(() -> new AppException(UserErrorCode.REFRESH_TOKEN_INVALID));
    }

    private UserDetails toUserDetails(User user) {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword() != null ? user.getPassword() : "",
                authorities
        );
    }
}
