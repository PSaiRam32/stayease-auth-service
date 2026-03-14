package com.stayease.auth_service.exception;


public class RefreshTokenException extends RuntimeException {

    public RefreshTokenException(String message){
        super(message);
    }

}