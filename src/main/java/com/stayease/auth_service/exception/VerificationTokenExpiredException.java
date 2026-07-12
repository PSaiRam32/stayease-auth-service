package com.stayease.auth_service.exception;

import lombok.extern.slf4j.Slf4j;
@Slf4j

public class VerificationTokenExpiredException extends RuntimeException {

    public VerificationTokenExpiredException(String message) {
        super(message);
        log.error("Verification Token Expired Exception: {}", message);
    }

}