package com.stayease.auth_service.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message){
        super(message);
        log.error("Invalid Refresh Token Exception: {}", message);
    }
}