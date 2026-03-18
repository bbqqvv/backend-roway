package org.bbqqvv.backendecommerce.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.JwtTokenUtil;
import org.bbqqvv.backendecommerce.entity.AuthProvider;
import org.bbqqvv.backendecommerce.entity.Role;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OAuth2Service implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String googleUserInfoUrl;

    public OAuth2Service(UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Transactional
    public String loginWithFacebook(String facebookToken) {
        log.info("Validating Facebook token...");

        // Mock bypass for testing
        if ("mock-facebook-token".equals(facebookToken)) {
            log.info("Mock Facebook token detected. Bypassing validation.");
            return handleSuccessfulOAuth2Login("fb-mock@example.com", "Mock FB User", "mock-fb-sub-456", AuthProvider.FACEBOOK, null);
        }

        // Thực tế sẽ gọi API Facebook: https://graph.facebook.com/me?fields=id,name,email,picture.type(large)&access_token=...
        String validationUrl = "https://graph.facebook.com/me?fields=id,name,email,picture.type(large)&access_token=" + facebookToken;
        ResponseEntity<Map> validationResponse;

        try {
            validationResponse = restTemplate.getForEntity(validationUrl, Map.class);
        } catch (Exception e) {
            log.error("Failed to validate Facebook token: {}", e.getMessage());
            throw new RuntimeException("Invalid Facebook token", e);
        }

        if (!validationResponse.getStatusCode().is2xxSuccessful() || validationResponse.getBody() == null) {
            log.error("Facebook token validation failed");
            throw new RuntimeException("Invalid Facebook token");
        }

        Map<String, Object> fbUser = validationResponse.getBody();
        String email = (String) fbUser.get("email");
        String name = (String) fbUser.get("name");
        String fbId = (String) fbUser.get("id");
        
        String avatarUrl = null;
        if (fbUser.containsKey("picture")) {
            Map<String, Object> picture = (Map<String, Object>) fbUser.get("picture");
            if (picture.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) picture.get("data");
                avatarUrl = (String) data.get("url");
            }
        }

        return handleSuccessfulOAuth2Login(email, name, fbId, AuthProvider.FACEBOOK, avatarUrl);
    }

    @Transactional
    public String loginWithGoogle(String googleToken) {
        log.info("Validating Google token...");

        // Gửi request xác minh token với Google
        if ("mock-google-token".equals(googleToken)) {
            log.info("Mock Google token detected. Bypassing validation.");
            return handleSuccessfulOAuth2Login("mock@example.com", "Mock User", "mock-sub-123", AuthProvider.GOOGLE, null);
        }

        String validationUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + googleToken;
        ResponseEntity<Map> validationResponse;

        try {
            validationResponse = restTemplate.getForEntity(validationUrl, Map.class);
        } catch (Exception e) {
            log.error("Failed to validate Google token: {}", e.getMessage());
            throw new RuntimeException("Invalid Google token", e);
        }

        // Kiểm tra phản hồi
        if (!validationResponse.getStatusCode().is2xxSuccessful() || validationResponse.getBody() == null) {
            log.error("Google token validation failed");
            throw new RuntimeException("Invalid Google token");
        }

        // Lấy thông tin user từ token
        Map<String, Object> googleUser = validationResponse.getBody();
        String email = (String) googleUser.get("email");
        String name = (String) googleUser.get("name");
        String googleId = (String) googleUser.get("sub");
        String avatarUrl = (String) googleUser.get("picture");

        return handleSuccessfulOAuth2Login(email, name, googleId, AuthProvider.GOOGLE, avatarUrl);
    }

    @Transactional
    protected String handleSuccessfulOAuth2Login(String email, String name, String providerId, AuthProvider provider, String avatarUrl) {
        if (email == null || providerId == null) {
            log.error("{} token missing required fields", provider);
            throw new RuntimeException("Invalid " + provider + " user data");
        }

        // Kiểm tra user trong DB hoặc tạo mới
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.info("Creating new user from {} login: {}", provider, email);
            user = User.builder()
                    .email(email)
                    .username(email) // Dùng email làm username để tránh trùng lặp
                    .name(name)
                    .provider(provider)
                    .providerId(providerId)
                    .avatarUrl(avatarUrl)
                    .authorities(Set.of(Role.ROLE_USER)) // Mặc định role USER
                    .build();
            userRepository.save(user);
        } else {
            // Cập nhật thông tin nếu có thay đổi
            boolean updated = false;
            if (user.getProviderId() == null) {
                user.setProviderId(providerId);
                updated = true;
            }
            if (user.getProvider() == AuthProvider.LOCAL || user.getProvider() == null) {
                log.info("User {} đã đăng ký bằng email/password trước, cập nhật provider thành {}.", email, provider);
                user.setProvider(provider);
                updated = true;
            }
            if (avatarUrl != null && (user.getAvatarUrl() == null || !avatarUrl.equals(user.getAvatarUrl()))) {
                user.setAvatarUrl(avatarUrl);
                updated = true;
            }
            if (updated) {
                userRepository.save(user);
            }
        }

        // Tạo UserDetails từ user
        UserDetails userDetails = this.loadUserByUsername(email);

        // Tạo JWT token và trả về
        String token = jwtTokenUtil.generateToken(userDetails);
        log.info("Google login successful for email: {}", email);
        return token;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "", // Không sử dụng password với OAuth2
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
            return Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_USER.name()));
        }
        return user.getAuthorities().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }
}
