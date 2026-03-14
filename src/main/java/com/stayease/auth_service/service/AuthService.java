package com.stayease.auth_service.service;

import com.stayease.auth_service.dto.AuthResponse;
import com.stayease.auth_service.dto.RegisterRequest;
import com.stayease.auth_service.entity.RefreshToken;
import com.stayease.auth_service.exception.InvalidCredentialsException;
import com.stayease.auth_service.exception.RefreshTokenException;
import com.stayease.auth_service.exception.RefreshTokenExpiredException;
import com.stayease.auth_service.exception.UserNotFoundException;
import com.stayease.auth_service.repository.RefreshTokenRepository;
import com.stayease.auth_service.repository.RoleRepository;
import com.stayease.auth_service.repository.UserRepository;
import com.stayease.auth_service.security.JwtUtil;
import com.stayease.auth_service.entity.User;
import com.stayease.auth_service.entity.Role;
import com.stayease.auth_service.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(@Valid RegisterRequest request){
        Role role=roleRepository.findByName(request.getRole())
                .orElseThrow();
        User user=new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password");
        }
        String accessToken = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getName()
        );
        String refreshTokenValue = jwtUtil.generateRefreshToken(user.getEmail());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(604800));
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(accessToken, refreshTokenValue);
    }

    public AuthResponse refreshToken(String refreshToken){
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));
        if(storedToken.getExpiryDate().isBefore(Instant.now())){
            throw new RefreshTokenExpiredException("Refresh token expired");
        }
        User user = storedToken.getUser();
        String newAccessToken = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getName()
        );
        return new AuthResponse(newAccessToken, refreshToken);
    }
}