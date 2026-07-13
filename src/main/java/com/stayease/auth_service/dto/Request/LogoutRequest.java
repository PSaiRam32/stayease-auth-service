package com.stayease.auth_service.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutRequest{
    @NotBlank(message = "Refresh Token is required")
    private String refreshToken;
}