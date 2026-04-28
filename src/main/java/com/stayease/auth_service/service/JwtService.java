package com.stayease.auth_service.service;

import com.stayease.auth_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class JwtService {

        private String secret="stayease-super-secret-key-for-jwt-authentication-2026-secure-key";

        // 60 minutes
        private final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 60;
        // 7 days
        private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7;

        public String generateAccessToken(User user) {
            log.debug("Generating access token for user ID: {}, role: {}", user.getUserId(), user.getRole());
            String token = Jwts.builder()
                    .setSubject(String.valueOf(user.getUserId()))
                    .claim("role", user.getRole().name())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                    .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                    .compact();
            log.debug("Access token generated successfully for user ID: {}", user.getUserId());
            return token;
        }
        public String generateRefreshToken(User user) {
            log.debug("Generating refresh token for user ID: {}", user.getUserId());
            String token = Jwts.builder()
                    .setSubject(String.valueOf(user.getUserId()))
                    .claim("type", "refresh")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                    .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                    .compact();
            log.debug("Refresh token generated successfully for user ID: {}", user.getUserId());
            return token;
        }
        public Claims validateToken(String token) {
            log.debug("Validating token");
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(secret.getBytes())
                        .parseClaimsJws(token)
                        .getBody();
                log.debug("Token validated successfully for user ID: {}", claims.getSubject());
                return claims;
            } catch (Exception e) {
                log.error("Token validation failed: {}", e.getMessage());
                throw e;
            }
        }
    }