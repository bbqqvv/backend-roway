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
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest creationRequest) {
        UserResponse userResponse = authenticationService.register(creationRequest);
        return ApiResponse.success(userResponse, "User registered successfully");
    }

    // Đăng nhập người dùng
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập truyền thống", description = "Xác thực người dùng bằng username và password, trả về JWT token.")
    public ApiResponse<JwtResponse> login(@RequestBody @Valid AuthenticationRequest authenticationRequest) {
        String token = authenticationService.login(authenticationRequest);
        return ApiResponse.success(new JwtResponse(token), "Login successful");
    }


    // 🔹 Đăng nhập bằng Google OAuth2
    @PostMapping("/oauth2/google")
    @Operation(summary = "Đăng nhập Google", description = "Xác thực và đăng nhập bằng Google ID Token.")
    public ApiResponse<JwtResponse> googleLogin(@RequestBody @Valid OAuth2LoginRequest request) {
        String jwtToken = oAuth2Service.loginWithGoogle(request.getToken());
        return ApiResponse.success(new JwtResponse(jwtToken), "Google login successful");
    }

    // 🔹 Đăng nhập bằng Facebook OAuth2
    @PostMapping("/oauth2/facebook")
    @Operation(summary = "Đăng nhập Facebook", description = "Xác thực và đăng nhập bằng Facebook Access Token.")
    public ApiResponse<JwtResponse> facebookLogin(@RequestBody @Valid OAuth2LoginRequest request) {
        String jwtToken = oAuth2Service.loginWithFacebook(request.getToken());
        return ApiResponse.success(new JwtResponse(jwtToken), "Facebook login successful");
    }

    /**
     * Gửi OTP để đặt lại mật khẩu
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi mã OTP đến email của người dùng để thực hiện khôi phục mật khẩu.")
    public ApiResponse<OtpResponse> forgotPassword(@RequestBody @Valid OtpRequest request) {
        String message = otpService.sendOtp(request.getEmail());
        return ApiResponse.success(new OtpResponse(message, request.getEmail()), "OTP sent successfully");
    }

    /**
     * Xác thực OTP
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Xác thực OTP", description = "Kiểm tra mã OTP người dùng gửi lên có khớp với mã hệ thống đã gửi qua email không.")
    public ApiResponse<OtpVerificationResponse> verifyOtp(@RequestBody @Valid OtpVerificationRequest request) {
        String result = otpService.verifyOtp(request.getEmail(), request.getOtp());
        boolean success = result.equals("OTP verified successfully!");
        return ApiResponse.success(new OtpVerificationResponse(result, success), result);
    }

    /**
     * Đặt lại mật khẩu sau khi xác thực OTP thành công
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu", description = "Cập nhật mật khẩu mới sau khi đã xác thực OTP thành công.")
    public ApiResponse<ResetPasswordResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        String result = otpService.resetPassword(request.getEmail(), request.getNewPassword());
        boolean success = result.equals("Password reset successful!");
        return ApiResponse.success(new ResetPasswordResponse(result, success), result);
    }
}
