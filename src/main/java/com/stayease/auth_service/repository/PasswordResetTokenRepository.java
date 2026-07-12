package com.stayease.auth_service.repository;

import com.stayease.auth_service.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByOtp(String otp);
    Optional<PasswordResetToken> findByUserUserId(Long userId);
}