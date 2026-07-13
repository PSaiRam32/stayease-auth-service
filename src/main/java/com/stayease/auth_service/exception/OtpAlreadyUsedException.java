package com.stayease.auth_service.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OtpAlreadyUsedException extends RuntimeException{
    public OtpAlreadyUsedException(String message){
        super(message);
        log.error("Otp Already Used Exception: {}", message);
    }
}
