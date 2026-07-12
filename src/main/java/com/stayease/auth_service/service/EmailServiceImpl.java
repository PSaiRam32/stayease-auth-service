package com.stayease.auth_service.service;

import com.stayease.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    @Value("${server.port}")
    private String serverPort;

    @Override
    public void sendVerificationEmail(User user, String token) {
        String verificationLink =
                "http://localhost:"
                        + serverPort
                        + "/auth/verify-email?token="
                        + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("StayEase Email Verification");
        message.setText(
                "Hello " + user.getName() + ",\n\n"
                        + "Thank you for registering with StayEase.\n\n"
                        + "Please click the link below to verify your email:\n\n"
                        + verificationLink
                        + "\n\n"
                        + "This link will expire in 24 hours.\n\n"
                        + "Regards,\n"
                        + "StayEase Team");
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetOtp(User user, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("StayEase Password Reset OTP");
        message.setText(
                "Hello " + user.getName()
                        + "\n\n"
                        + "Your OTP for password reset is : "
                        + otp
                        + "\n\n"
                        + "This OTP is valid for 10 minutes."
                        + "\n\n"
                        + "Regards,\nStayEase Team"
        );
        mailSender.send(message);
    }
}