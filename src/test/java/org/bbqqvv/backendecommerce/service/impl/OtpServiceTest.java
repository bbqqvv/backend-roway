package org.bbqqvv.backendecommerce.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private UserRepository userRepository;
    @Mock private JavaMailSender mailSender;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TemplateEngine templateEngine;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks
    private OtpServiceImpl otpService;

    private final String email = "test@example.com";
    private final String otp = "123456";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Gửi OTP thành công")
    void sendOtp_shouldSaveToRedisAndSendEmail() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("Email Content");

        String result = otpService.sendOtp(email);

        assertThat(result).isEqualTo("OTP sent successfully!");
        verify(valueOperations).set(eq("otp:" + email), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Xác thực OTP thành công")
    void verifyOtp_shouldReturnSuccess_whenOtpMatches() {
        when(valueOperations.get("otp:" + email)).thenReturn(otp);

        String result = otpService.verifyOtp(email, otp);

        assertThat(result).isEqualTo("OTP verified successfully!");
        verify(redisTemplate).delete("otp:" + email);
    }

    @Test
    @DisplayName("Xác thực OTP thất bại - Sai mã")
    void verifyOtp_shouldReturnError_whenOtpIsInvalid() {
        when(valueOperations.get("otp:" + email)).thenReturn(otp);

        String result = otpService.verifyOtp(email, "654321");

        assertThat(result).isEqualTo("Invalid OTP!");
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("Xác thực OTP thất bại - Hết hạn")
    void verifyOtp_shouldReturnError_whenOtpIsExpired() {
        when(valueOperations.get("otp:" + email)).thenReturn(null);

        String result = otpService.verifyOtp(email, otp);

        assertThat(result).isEqualTo("OTP expired or not found!");
    }

    @Test
    @DisplayName("Reset Password sau khi verify OTP")
    void resetPassword_shouldUpdateUserPassword() {
        User user = User.builder().email(email).build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedPass");

        String result = otpService.resetPassword(email, "newPass123");

        assertThat(result).isEqualTo("Password reset successful!");
        assertThat(user.getPassword()).isEqualTo("encodedPass");
        verify(userRepository).save(user);
    }
}
