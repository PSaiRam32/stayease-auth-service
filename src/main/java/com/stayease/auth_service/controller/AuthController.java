package com.stayease.auth_service.controller;

import com.stayease.auth_service.dto.*;
import com.stayease.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth Controller", description = "Authentication APIs")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(summary="Register new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        log.info("POST /auth/register - Registration request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        log.info("POST /auth/register - Registration successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary="Login user and obtain JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("POST /auth/login - Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("POST /auth/login - Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh Access Token")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshAccessToken(@RequestBody RefreshTokenRequest token) {
        log.info("POST /auth/refresh-token - Token refresh request received");
        AuthResponse response = authService.refreshToken(token.getRefreshToken());
        log.info("POST /auth/refresh-token - Token refresh successful");
        return ResponseEntity.ok(response);
    }

    @Operation(summary="Change Password - Update user password")
    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("POST /auth/change-password - Request received for email: {}", request.getEmail());
        ChangePasswordResponse response = authService.changePassword(request);
        log.info("POST /auth/change-password - Password changed successfully for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(summary="Verify Email")
    @GetMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestParam String token) {
        log.info("GET /auth/verify-email - Email verification request received");
        authService.verifyEmail(token);
        log.info("GET /auth/verify-email - Email verified successfully");
        return ResponseEntity.ok(AuthResponse.builder().message("Email verified successfully. You can now login.").build());
    }

    @Operation(summary="Forgot Password")
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        log.info("POST /auth/forgot-password - Request received for email: {}",request.getEmail());
        authService.forgotPassword(request);
        log.info("POST /auth/forgot-password - OTP sent successfully for email: {}", request.getEmail());
        return ResponseEntity.ok(AuthResponse.builder().message("Password reset OTP sent successfully.").build());
    }

    @Operation(summary = "Reset Password")
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /auth/reset-password - Password reset request received for email: {}", request.getEmail());
        authService.resetPassword(request);
        log.info("POST /auth/reset-password - Password reset successful for email: {}", request.getEmail());
        return ResponseEntity.ok(AuthResponse.builder()
                .message("Password Reset Successful").build());
    }
}