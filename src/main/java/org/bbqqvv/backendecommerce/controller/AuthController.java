package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.request.*;
import org.bbqqvv.backendecommerce.dto.response.*;
import org.bbqqvv.backendecommerce.service.OtpService;
import org.bbqqvv.backendecommerce.service.auth.AuthenticationService;
import org.bbqqvv.backendecommerce.service.auth.OAuth2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthenticationService authenticationService;
    OtpService otpService;
    OAuth2Service oAuth2Service;
    public AuthController(AuthenticationService authenticationService, OtpService otpService, OAuth2Service oAuth2Service) {
        this.authenticationService = authenticationService;
        this.otpService = otpService;
        this.oAuth2Service = oAuth2Service;
    }

    // Đăng ký người dùng
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserCreationRequest creationRequest) {
        UserResponse userResponse = authenticationService.register(creationRequest);
        return userResponse != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(userResponse)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new UserResponse("Registration failed"));
    }

    // Đăng nhập người dùng
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationRequest authenticationRequest) {
        try {
            String token = authenticationService.login(authenticationRequest);
            return ResponseEntity.ok(new JwtResponse(token)); // Trả về JWT token
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
    }


    // 🔹 Đăng nhập bằng Google OAuth2 (Frontend sẽ redirect đến URL này)
    @PostMapping("/oauth2/google")
    public ResponseEntity<?> googleLogin(@RequestBody @Valid OAuth2LoginRequest request) {
        try {
            String jwtToken = oAuth2Service.loginWithGoogle(request.getToken());
            return ResponseEntity.ok(new JwtResponse(jwtToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google login failed: " + e.getMessage());
        }
    }

    /**
     * Gửi OTP để đặt lại mật khẩu
     */
    @PostMapping("/forgot-password")
    public ApiResponse<OtpResponse> forgotPassword(@RequestBody @Valid OtpRequest request) {
        String message = otpService.sendOtp(request.getEmail());
        return ApiResponse.<OtpResponse>builder()
                .success(true)
                .message("OTP sent successfully")
                .data(new OtpResponse(message, request.getEmail()))
                .build();
    }

    /**
     * Xác thực OTP
     */
    @PostMapping("/verify-otp")
    public ApiResponse<OtpVerificationResponse> verifyOtp(@RequestBody @Valid OtpVerificationRequest request) {
        String result = otpService.verifyOtp(request.getEmail(), request.getOtp());
        boolean success = result.equals("OTP verified successfully!");

        return ApiResponse.<OtpVerificationResponse>builder()
                .success(success)
                .message(result)
                .data(new OtpVerificationResponse(result, success))
                .build();
    }

    /**
     * Đặt lại mật khẩu sau khi xác thực OTP thành công
     */
    @PostMapping("/reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        String result = otpService.resetPassword(request.getEmail(), request.getNewPassword());
        boolean success = result.equals("Password reset successful!");

        return ApiResponse.<ResetPasswordResponse>builder()
                .success(success)
                .message(result)
                .data(new ResetPasswordResponse(result, success))
                .build();
    }
}
