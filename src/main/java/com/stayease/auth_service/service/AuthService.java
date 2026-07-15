package com.stayease.auth_service.service;


import com.stayease.auth_service.dto.Request.*;
import com.stayease.auth_service.dto.Response.AuthResponse;
import com.stayease.auth_service.dto.Response.ChangePasswordResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    ChangePasswordResponse changePassword(ChangePasswordRequest request);
    void verifyEmail(String token);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void logout(LogoutRequest request);
    void deactivateUser(Long userId,UserDeactivationRequest request);
}