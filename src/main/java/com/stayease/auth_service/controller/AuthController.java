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
        try {
            AuthResponse response = authService.register(request);
            log.info("POST /auth/register - Registration successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("POST /auth/register - Registration failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @Operation(summary="Login user and obtain JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("POST /auth/login - Login request received for email: {}", request.getEmail());
        try {
            AuthResponse response = authService.login(request);
            log.info("POST /auth/login - Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("POST /auth/login - Login failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @Operation(summary = "Refresh Access Token")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        log.info("POST /auth/refresh-token - Token refresh request received");
        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            log.info("POST /auth/refresh-token - Token refresh successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("POST /auth/refresh-token - Token refresh failed", e);
            throw e;
        }
    }

    @Operation(summary="Change Password - Update user password")
    @PostMapping("/change-password")
    public ChangePasswordResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request for user: {}", request.getEmail());
        return authService.changePassword(request);
    }
}