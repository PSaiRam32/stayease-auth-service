package com.stayease.auth_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVerificationRequest {
    private boolean active;
    private boolean emailVerified;
}