package com.stayease.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 1000)
    private String token;
    //A user may log in from Laptop,Mobile,Tablet - Each device should have its own refresh token.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private LocalDateTime expiryTime;
    @Builder.Default
    private boolean revoked = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
