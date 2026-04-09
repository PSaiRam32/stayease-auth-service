package com.stayease.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String message;
    private Long userId;
    private String name;
    private String role;
    private String accessToken;
    private String refreshToken;
}