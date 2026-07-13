package com.stayease.auth_service.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OtpExpiredException extends RuntimeException{
    public OtpExpiredException(String message){
        super(message);
        log.error("OTP Expired Exception: {}", message);
    }
}
