package com.stayease.auth_service.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException(String message) {
        super(message);
        log.error("Email Not Verified Exception: {}", message);
    }

}