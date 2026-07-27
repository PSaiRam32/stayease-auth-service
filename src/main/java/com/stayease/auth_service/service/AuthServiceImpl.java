package com.stayease.auth_service.service;

import com.stayease.auth_service.config.OwnerClient;
import com.stayease.auth_service.dto.Request.*;
import com.stayease.auth_service.dto.Response.AuthResponse;
import com.stayease.auth_service.dto.Response.ChangePasswordResponse;
import com.stayease.auth_service.entity.*;
import com.stayease.auth_service.config.UserClient;
import com.stayease.auth_service.exception.*;
import com.stayease.auth_service.repository.EmailVerificationTokenRepository;
import com.stayease.auth_service.repository.PasswordResetTokenRepository;
import com.stayease.auth_service.repository.RefreshTokenRepository;
import com.stayease.auth_service.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserClient userClient;
    private final OwnerClient ownerClient;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final int PASSWORD_RESET_OTP_EXPIRY_MINUTES = 10;
    private static final int EMAIL_VERIFICATION_EXPIRY_HOURS = 24;

    @Transactional
    public AuthResponse register(RegisterRequest request){
        log.info("Starting user registration for email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration attempt for existing email: {}", request.getEmail());
            throw new RuntimeException("User already exists");
        }
        Role assignedRole=Role.valueOf(request.getRole());
        log.debug("Assigned role from request: {}", request.getRole());
        if (assignedRole==null){
            assignedRole=Role.ROLE_USER;
            log.debug("Role was null, defaulting to ROLE_USER");
        }
        if (assignedRole==Role.ROLE_ADMIN){
            log.warn("Attempted registration with ROLE_ADMIN for email: {}", request.getEmail());
            throw new RuntimeException("Admin registration is not allowed");
        }
        User user=User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(assignedRole)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(false)
                 .emailVerified(false).build();
        log.info("Saving user to database with email: {}, role: {}", request.getEmail(), assignedRole);
        User savedUser=userRepository.save(user);
        String verificationToken=generateVerificationToken();
        EmailVerificationToken emailToken=EmailVerificationToken.builder()
                        .token(verificationToken)
                        .user(savedUser)
                        .createdAt(LocalDateTime.now())
                        .expiryTime(LocalDateTime.now().plusHours(EMAIL_VERIFICATION_EXPIRY_HOURS))
                        .used(false)
                        .build();
        emailVerificationTokenRepository.save(emailToken);
        log.info("Email verification token generated for user ID: {}", savedUser.getUserId());
        boolean userServiceCreated=false;
        log.info("User saved successfully with ID: {}", savedUser.getUserId());
        try {
            log.info("Calling user service to create user profile for ID: {}", user.getUserId());
            userClient.createUser(new UserProfileRequest(
                            savedUser.getUserId(),
                            savedUser.getName(),
                            savedUser.getEmail(),
                            savedUser.getRole(),
                            savedUser.getPhone(),
                            savedUser.getCreatedAt(),
                            savedUser.getUpdatedAt(),
                            savedUser.isActive(),
                            savedUser.isEmailVerified()
                    )
            );
            userServiceCreated=true;
            log.info("User profile created successfully in user service for ID: {}", user.getUserId());
            log.info("Sending verification email to {}", savedUser.getEmail());
            emailService.sendVerificationEmail(savedUser, verificationToken);
            log.info("Verification email sent successfully to {}", savedUser.getEmail());
            if(savedUser.getRole()==Role.ROLE_OWNER){
                ownerClient.createOwner(
                        new OwnerCreateRequest(
                                savedUser.getUserId(),
                                savedUser.getName(),
                                savedUser.getEmail(),
                                savedUser.getPhone(),
                                savedUser.getCreatedAt(),
                                savedUser.getUpdatedAt()
                        )
                );
                log.info("Owner profile created successfully in Owner service for ID: {}", user.getUserId());
            }
        } catch (Exception ex){
            if (userServiceCreated){
                try {
                    userClient.deleteUser(savedUser.getUserId());
                } catch (Exception e) {
                    log.error("User Service rollback failed", e);
                }
            }
            try {
                log.warn("Rolling back email verification token for user ID: {}", savedUser.getUserId());
                emailVerificationTokenRepository.delete(emailToken);
                log.warn("Rolling back Auth user with ID: {}", savedUser.getUserId());
                userRepository.deleteById(savedUser.getUserId());
            }
            catch (Exception e){
                log.error("Auth rollback failed", e);
            }
            throw new RuntimeException("Registration failed. Rolled back.", ex);
        }
        log.info("User registration completed successfully for email: {}", request.getEmail());
        return AuthResponse.builder()
                .message("Registration successful. Please verify your email before logging in.")
                .userId(user.getUserId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request){
        log.info("Starting login attempt for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });
        if (!user.isActive()){
            log.warn("Login denied. Email not verified for user: {}", request.getEmail());
            throw new EmailNotVerifiedException("Please verify your email before logging in.");
        }
        log.debug("User found in database with email: {}, ID: {}", request.getEmail(), user.getUserId());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            log.warn("Login failed: Invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        log.debug("Password validation successful for email: {}", request.getEmail());
        log.info("Generating access token for user ID: {}", user.getUserId());
        String accessToken=jwtService.generateAccessToken(user);
        log.debug("Access token generated successfully");
        log.info("Generating refresh token for user ID: {}", user.getUserId());
        String refreshToken=jwtService.generateRefreshToken(user);
        refreshTokenRepository.deleteByUserUserId(user.getUserId());
        saveRefreshToken(user,refreshToken);
        log.debug("Refresh token generated successfully");
        log.info("Login successful for email: {}",request.getEmail());
        return AuthResponse.builder()
                .message("Login successful")
                .userId(user.getUserId())
                .name(user.getName())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken){
        log.info("Starting refresh token process");
        Claims claims=jwtService.validateToken(refreshToken);
        Long userId=Long.parseLong(claims.getSubject());
        RefreshToken storedToken=refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
        if (storedToken.isRevoked()){
            throw new RefreshTokenRevokedException("Refresh token has been revoked");
        }
        if (storedToken.getExpiryTime().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(storedToken);
            throw new RefreshTokenExpiredException("Refresh token expired");
        }
        User user=userRepository.findById(userId).orElseThrow(() ->
                        new UserNotFoundException("User not found"));
        String newAccessToken=jwtService.generateAccessToken(user);
        //RefreshTokenRotation
        String newRefreshToken=jwtService.generateRefreshToken(user);
        refreshTokenRepository.delete(storedToken);
        RefreshToken rf=saveRefreshToken(user, newRefreshToken);
        log.info("Refresh token rotated successfully");
        return AuthResponse.builder()
                .message("Token refreshed successfully")
                .userId(user.getUserId())
                .name(user.getName())
                .role(user.getRole().name())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public ChangePasswordResponse changePassword(ChangePasswordRequest request){
        log.info("Processing change password request for user: {}", request.getEmail());
        // Validate new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())){
            log.error("New password and confirm password do not match for user: {}", request.getEmail());
            throw new RuntimeException("New password and confirm password do not match");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", request.getEmail());
                    return new UserNotFoundException("User not found with id: " + request.getEmail());
                });
        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            log.warn("Old password is incorrect for user: {}", request.getEmail());
            throw new RuntimeException("Old password is incorrect");
        }
        // Validate new password is not same as old password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())){
            log.warn("New password cannot be same as old password for user: {}", request.getEmail());
            throw new RuntimeException("New password cannot be same as old password");
        }
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", request.getEmail());
        return new ChangePasswordResponse(true, "Password changed successfully");
    }

    @Override
    @Transactional
    public void verifyEmail(String token){
        log.info("Starting email verification");
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                        .orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));
        log.debug("Verification token found successfully");
        if (verificationToken.isUsed()){
            throw new EmailAlreadyVerifiedException("Email already verified");
        }
        if (verificationToken.getExpiryTime().isBefore(LocalDateTime.now())){
            throw new VerificationTokenExpiredException("Verification token expired");
        }
        log.debug("Verification token is valid and not expired");
        User user=verificationToken.getUser();
        user.setActive(true);
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User {} verified successfully", user.getEmail());
//        verificationToken.setUsed(true);
        log.info("Synchronizing verification status with User Service");
        userClient.verifyUser(user.getUserId(),UserVerificationRequest.builder()
                        .active(true)
                        .emailVerified(true)
                        .build());
        log.info("User Service synchronized successfully");
        log.info("Synchronizing verification status with Owner Service");
        if(user.getRole()==Role.ROLE_OWNER){
            ownerClient.verifyOwner(user.getUserId(),UserVerificationRequest.builder()
                            .active(true)
                            .emailVerified(true)
                            .build());
        }
        log.info("Owner Service synchronized successfully");
        emailVerificationTokenRepository.delete(verificationToken);
        log.info("Verification token removed");
        log.info("Email verification completed successfully for user: {}", user.getEmail());
//        emailVerificationTokenRepository.save(verificationToken);
    }

    private String generateVerificationToken(){
        return UUID.randomUUID().toString();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request){
        log.info("Forgot password initiated for email: {}", request.getEmail());
        User user=userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
                        new UserNotFoundException("User not found"));
        //Remove Old Reset Tokens
        passwordResetTokenRepository.findByUserUserId(user.getUserId())
                .ifPresent(passwordResetTokenRepository::delete);
        String otp = generateOtp();
        PasswordResetToken resetToken=PasswordResetToken.builder()
                        .otp(otp)
                        .user(user)
                        .createdAt(LocalDateTime.now())
                        .expiryTime(LocalDateTime.now().plusMinutes(PASSWORD_RESET_OTP_EXPIRY_MINUTES))
                        .used(false)
                        .build();
        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset OTP generated for user {}", user.getEmail());
        emailService.sendPasswordResetOtp(user, otp);
        log.info("Password reset OTP email sent");
    }
    private PasswordResetToken validateOtp(String otp, String email){
        log.info("Verifying password reset OTP");
        PasswordResetToken token=passwordResetTokenRepository.findByOtp(otp)
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));
        if(!token.getUser().getEmail().equals(email)){
            throw new InvalidOtpException("Invalid OTP");
        }
        if(token.isUsed()){
            throw new OtpAlreadyUsedException("OTP already used");
        }
        if(token.getExpiryTime().isBefore(LocalDateTime.now())){
            throw new OtpExpiredException("OTP expired");
        }
        log.info("OTP verified successfully");
        return token;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request){
        log.info("Reset password started");
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("Passwords do not match");
        }
        PasswordResetToken token=validateOtp(request.getOtp(), request.getEmail());
        User user=token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        passwordResetTokenRepository.delete(token);
        log.info("Password reset successful for user {}", user.getEmail());
    }


    private String generateOtp(){
        return String.valueOf(java.util.concurrent.ThreadLocalRandom.current().nextInt(100000,1000000));
    }

    private RefreshToken saveRefreshToken(User user, String token){
        RefreshToken refreshToken=RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryTime(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request){
        log.info("Logout initiated");
        jwtService.validateToken(request.getRefreshToken());
        RefreshToken refreshToken=refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
        if(refreshToken.isRevoked()){
            throw new RefreshTokenRevokedException("Refresh token already revoked");
        }
        refreshToken.setRevoked(true);
        refreshToken.setUpdatedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
        log.info("User logged out successfully");
    }

    @Override
    @Transactional
    public void deactivateUser(Long userId,UserDeactivationRequest request){
        log.info("Received user deactivation request for userId: {}", userId);
        User user=userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));
        user.setActive(request.isActive());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User {} deactivated successfully.", userId);
    }
}