package com.stayease.auth_service.controller;


import com.stayease.auth_service.dto.AuthResponse;
import com.stayease.auth_service.dto.LoginRequest;
import com.stayease.auth_service.dto.RefreshTokenRequest;
import com.stayease.auth_service.dto.RegisterRequest;
import com.stayease.auth_service.entity.User;
import com.stayease.auth_service.security.JwtUtil;
import com.stayease.auth_service.service.AuthService;
import com.stayease.auth_service.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request){
        authService.register(request);
        return "User registered successfully";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/refresh-token")
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest request){
        return authService.refreshToken(request.getRefreshToken());
    }
}