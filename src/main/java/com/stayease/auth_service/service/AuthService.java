package com.stayease.auth_service.service;

import com.stayease.auth_service.config.OwnerClient;
import com.stayease.auth_service.dto.*;
import com.stayease.auth_service.entity.Role;
import com.stayease.auth_service.config.UserClientConfig;
import com.stayease.auth_service.entity.User;
import com.stayease.auth_service.exception.InvalidCredentialsException;
import com.stayease.auth_service.exception.RefreshTokenExpiredException;
import com.stayease.auth_service.exception.UserNotFoundException;
import com.stayease.auth_service.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserClientConfig userClientConfig;
    private final OwnerClient ownerClient;

    public AuthResponse register(RegisterRequest request) {
        log.info("Starting user registration for email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration attempt for existing email: {}", request.getEmail());
            throw new RuntimeException("User already exists");
        }
        Role assignedRole = Role.valueOf(request.getRole());
        log.debug("Assigned role from request: {}", request.getRole());
        if (assignedRole == null) {
            assignedRole = Role.ROLE_USER;
            log.debug("Role was null, defaulting to ROLE_USER");
        }
        if (assignedRole == Role.ROLE_ADMIN) {
            log.warn("Attempted registration with ROLE_ADMIN for email: {}", request.getEmail());
            throw new RuntimeException("Admin registration is not allowed");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(assignedRole);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        log.info("Saving user to database with email: {}, role: {}", request.getEmail(), assignedRole);
        userRepository.save(user);
        log.info("User saved successfully with ID: {}", user.getId());
        try {
            log.info("Calling user service to create user profile for ID: {}", user.getId());
            userClientConfig.createUser(
                    new UserProfileRequest(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getRole(),
                            user.getPhone(),
                            user.getCreatedAt(),
                            user.getUpdatedAt()
                    )
            );
            log.info("User profile created successfully in user service for ID: {}", user.getId());
            if(user.getRole() == Role.ROLE_OWNER){
                ownerClient.createOwner(
                        new OwnerCreateRequest(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getPhone(),
                                user.getCreatedAt(),
                                user.getUpdatedAt()
                        )
                );
            }
            log.info("Owner profile created successfully in Owner service for ID: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to create user profile in user service for ID: {}. Rolling back user creation. Error: ", user.getId(), e);
            userRepository.delete(user);
            log.info("User rolled back from database for ID: {}", user.getId());
            throw new RuntimeException("User registration failed. Please try again.",e);
        }
        log.info("User registration completed successfully for email: {}", request.getEmail());
        return AuthResponse.builder()
                .message("User registered successfully")
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
    public AuthResponse login(LoginRequest request) {
        log.info("Starting login attempt for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });
        log.debug("User found in database with email: {}, ID: {}", request.getEmail(), user.getId());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        log.debug("Password validation successful for email: {}", request.getEmail());
        log.info("Generating access token for user ID: {}", user.getId());
        String accessToken = jwtService.generateAccessToken(user);
        log.debug("Access token generated successfully");
        log.info("Generating refresh token for user ID: {}", user.getId());
        String refreshToken = jwtService.generateRefreshToken(user);
        log.debug("Refresh token generated successfully");
        log.info("Login successful for email: {}", request.getEmail());
        return AuthResponse.builder()
                .message("Login successful")
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Starting token refresh process");
        log.debug("Validating refresh token");
        Claims claims = jwtService.validateToken(refreshToken);
        log.debug("Refresh token validated successfully");
        if(claims.getExpiration().before(Date.from(Instant.now()))){
            log.warn("Token refresh failed: Refresh token has expired");
            throw new RefreshTokenExpiredException("Refresh token expired");
        }
        log.debug("Token expiration check passed");
        Long userId = Long.parseLong(claims.getSubject());
        log.debug("Extracted user ID from token: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Token refresh failed: User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });
        log.debug("User found in database with ID: {}", userId);
        log.info("Generating new access token for user ID: {}", userId);
        String newAccessToken = jwtService.generateAccessToken(user);
        log.debug("New access token generated successfully");
        log.info("Token refresh completed successfully for user ID: {}", userId);
        return AuthResponse.builder()
                .message("Token refreshed successfully")
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        log.info("Processing change password request for user: {}", request.getEmail());
        // Validate new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.error("New password and confirm password do not match for user: {}", request.getEmail());
            throw new RuntimeException("New password and confirm password do not match");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", request.getEmail());
                    return new UserNotFoundException("User not found with id: " + request.getEmail());
                });
        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Old password is incorrect for user: {}", request.getEmail());
            throw new RuntimeException("Old password is incorrect");
        }
        // Validate new password is not same as old password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
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

}