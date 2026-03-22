package com.stayease.auth_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    private Long id;
    private String name;
    private String email;
    private String role;

}