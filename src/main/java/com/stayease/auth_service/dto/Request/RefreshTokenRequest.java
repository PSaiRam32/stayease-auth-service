package com.stayease.auth_service.dto.Request;

import lombok.Data;

@Data
public class RefreshTokenRequest{
    private String refreshToken;
}