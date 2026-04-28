package com.stayease.auth_service.service;


import com.stayease.auth_service.dto.*;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    ChangePasswordResponse changePassword(ChangePasswordRequest request);

}