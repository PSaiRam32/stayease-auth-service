package com.stayease.auth_service.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException(String message){
        super(message);
        log.error("Refresh Token Revoked Exception: {}", message);
    }
}
