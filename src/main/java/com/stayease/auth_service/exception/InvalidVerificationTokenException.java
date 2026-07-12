package com.stayease.auth_service.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidVerificationTokenException extends RuntimeException {

    public InvalidVerificationTokenException(String message) {
        super(message);
        log.error("Invalid Verification Token Exception: {}", message);
    }

}