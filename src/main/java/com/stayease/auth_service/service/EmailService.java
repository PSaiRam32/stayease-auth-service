package com.stayease.auth_service.service;

import com.stayease.auth_service.entity.User;

public interface EmailService {
    void sendVerificationEmail(User user, String token);
    void sendPasswordResetOtp(User user,String otp);
}