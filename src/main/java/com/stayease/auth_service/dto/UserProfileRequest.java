package com.stayease.auth_service.dto;

import com.stayease.auth_service.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}