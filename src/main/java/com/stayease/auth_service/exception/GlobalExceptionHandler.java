package com.stayease.auth_service.exception;

import com.stayease.auth_service.dto.Response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex){
        log.warn("UserNotFoundException caught: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex){
        log.warn("InvalidCredentialsException caught: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex){
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(RefreshTokenExpiredException ex){
        log.warn("RefreshTokenExpiredException caught: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex) {
        log.warn("EmailNotVerifiedException caught: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationToken(InvalidVerificationTokenException ex) {
        log.warn("InvalidVerificationTokenException caught: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VerificationTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleVerificationTokenExpired(VerificationTokenExpiredException ex) {
        log.warn("VerificationTokenExpiredException caught: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.GONE.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyVerified(EmailAlreadyVerifiedException ex) {
        log.warn("EmailAlreadyVerifiedException caught: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex){
        ErrorResponse error=new ErrorResponse(
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        LocalDateTime.now());
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpired(OtpExpiredException ex){
        ErrorResponse error=new ErrorResponse(
                        ex.getMessage(),
                        HttpStatus.GONE.value(),
                        LocalDateTime.now());
        return new ResponseEntity<>(error,HttpStatus.GONE);
    }

    @ExceptionHandler(OtpAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleOtpAlreadyUsed(OtpAlreadyUsedException ex){
        ErrorResponse error=new ErrorResponse(
                        ex.getMessage(),
                        HttpStatus.CONFLICT.value(),
                        LocalDateTime.now());
        return new ResponseEntity<>(error,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex){
        ErrorResponse error=new ErrorResponse(
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED.value(),
                        LocalDateTime.now());
        return new ResponseEntity<>(error,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ErrorResponse> handleRevokedRefreshToken(RefreshTokenRevokedException ex){
        ErrorResponse error=new ErrorResponse(
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED.value(),
                        LocalDateTime.now());
        return new ResponseEntity<>(error,HttpStatus.UNAUTHORIZED);
    }




}