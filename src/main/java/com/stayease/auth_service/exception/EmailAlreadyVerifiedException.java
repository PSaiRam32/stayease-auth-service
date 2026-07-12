package com.stayease.auth_service.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailAlreadyVerifiedException extends RuntimeException {

    public EmailAlreadyVerifiedException(String message) {
        super(message);
        log.error("Email Already Verified Exception: {}", message);
    }
}