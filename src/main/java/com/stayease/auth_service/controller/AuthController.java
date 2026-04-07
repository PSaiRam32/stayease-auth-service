package com.stayease.auth_service.controller;

import com.stayease.auth_service.dto.AuthResponse;
import com.stayease.auth_service.dto.LoginRequest;
import com.stayease.auth_service.dto.RegisterRequest;
import com.stayease.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

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
}