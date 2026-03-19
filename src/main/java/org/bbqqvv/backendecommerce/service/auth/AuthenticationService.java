package org.bbqqvv.backendecommerce.service.auth;

import org.bbqqvv.backendecommerce.config.jwt.JwtTokenUtil;
import org.bbqqvv.backendecommerce.dto.request.AuthenticationRequest;
import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.entity.AuthProvider;
import org.bbqqvv.backendecommerce.entity.Role;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.mapper.UserMapper;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.UserErrorCode;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.util.ValidateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;


@Service
public class AuthenticationService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationService(UserMapper userMapper,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager,
                                 JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
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

    public String login(AuthenticationRequest loginUserDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUserDto.getUsername(), loginUserDto.getPassword())
        );
        // Sửa lại dòng này để truyền UserDetails thay vì chỉ username
        return jwtTokenUtil.generateToken((UserDetails) authentication.getPrincipal());
    }
}
