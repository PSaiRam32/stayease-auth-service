package com.stayease.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String otp;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}