package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication", description = "Đăng ký, Đăng nhập và Quên mật khẩu")
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
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản người dùng mới với các thông tin cơ bản.")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserCreationRequest creationRequest) {
        UserResponse userResponse = authenticationService.register(creationRequest);
        return userResponse != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(userResponse)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new UserResponse("Registration failed"));
    }

    // Đăng nhập người dùng
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập truyền thống", description = "Xác thực người dùng bằng username và password, trả về JWT token.")
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


    // 🔹 Đăng nhập bằng Google OAuth2
    @PostMapping("/oauth2/google")
    @Operation(summary = "Đăng nhập Google", description = "Xác thực và đăng nhập bằng Google ID Token.")
    public ResponseEntity<?> googleLogin(@RequestBody @Valid OAuth2LoginRequest request) {
        try {
            String jwtToken = oAuth2Service.loginWithGoogle(request.getToken());
            return ResponseEntity.ok(new JwtResponse(jwtToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google login failed: " + e.getMessage());
        }
    }

    // 🔹 Đăng nhập bằng Facebook OAuth2
    @PostMapping("/oauth2/facebook")
    @Operation(summary = "Đăng nhập Facebook", description = "Xác thực và đăng nhập bằng Facebook Access Token.")
    public ResponseEntity<?> facebookLogin(@RequestBody @Valid OAuth2LoginRequest request) {
        try {
            String jwtToken = oAuth2Service.loginWithFacebook(request.getToken());
            return ResponseEntity.ok(new JwtResponse(jwtToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Facebook login failed: " + e.getMessage());
        }
    }

    /**
     * Gửi OTP để đặt lại mật khẩu
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi mã OTP đến email của người dùng để thực hiện khôi phục mật khẩu.")
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
    @Operation(summary = "Xác thực OTP", description = "Kiểm tra mã OTP người dùng gửi lên có khớp với mã hệ thống đã gửi qua email không.")
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
    @Operation(summary = "Đặt lại mật khẩu", description = "Cập nhật mật khẩu mới sau khi đã xác thực OTP thành công.")
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
