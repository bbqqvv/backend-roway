package org.bbqqvv.backendecommerce.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.service.OtpService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;

    private static final int OTP_EXPIRATION_MINUTES = 5;

    public OtpServiceImpl(
            RedisTemplate<String, String> redisTemplate,
            UserRepository userRepository,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder,
            TemplateEngine templateEngine
    ) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.templateEngine = templateEngine;
    }

    @Override
    public String sendOtp(String email) {
        String otp = generateOtp();
        redisTemplate.opsForValue().set(buildOtpKey(email), otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        try {
            sendOtpEmail(email, otp);
            return "OTP sent successfully!";
        } catch (MessagingException e) {
            log.error("Error sending OTP email: {}", e.getMessage());
            return "Failed to send OTP!";
        }
    }

    @Override
    public String verifyOtp(String email, String otpInput) {
        String key = buildOtpKey(email);
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) return "OTP expired or not found!";
        if (!savedOtp.equals(otpInput)) return "Invalid OTP!";

        redisTemplate.delete(key);
        return "OTP verified successfully!";
    }

    @Override
    public String resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Password reset successful!";
    }

    private String buildOtpKey(String email) {
        return "otp:" + email;
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void sendOtpEmail(String email, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[ROWAY] Mã xác thực OTP của bạn");

        // Load template and fill variables
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expiration", OTP_EXPIRATION_MINUTES);

        String content = templateEngine.process("otp-template", context);
        helper.setText(content, true);
        helper.addInline("logo", new ClassPathResource("static/images/roway.png"));

        mailSender.send(message);
    }
}
