package com.stayease.auth_service.integration;

import com.stayease.auth_service.config.UserClient;
import com.stayease.auth_service.dto.Request.UserProfileRequest;
import com.stayease.auth_service.dto.Request.UserVerificationRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceGateway {

    private final UserClient userClient;

    @Retry(name="user-service")
    @CircuitBreaker(name = "user-service",fallbackMethod = "createUserFallback")
    public void createUser(UserProfileRequest request){
        log.info("Calling User Service");
        userClient.createUser(request);
        log.info("User Service call successful");
    }

    public void createUserFallback(UserProfileRequest request,Exception ex){
        log.error("User Service unavailable", ex);
        throw new RuntimeException("User Service is currently unavailable while creating user.",ex);
    }

    @Retry(name = "user-service")
    @CircuitBreaker(name = "user-service",fallbackMethod = "verifyUserFallback")
    public void verifyUser(Long userId,UserVerificationRequest request){
        userClient.verifyUser(userId, request);
    }

    public void verifyUserFallback(Long userId,UserVerificationRequest request,Exception ex){
        log.error("Verification failed",ex);
        throw new RuntimeException("User Service is currently unavailable while verifying userId: " + userId,ex);
    }

    @Retry(name="user-service")
    @CircuitBreaker(name="user-service",fallbackMethod="deleteUserFallback")
    public void deleteUser(Long userId){
        userClient.deleteUser(userId);
    }

    public void deleteUserFallback(Long userId,Exception ex){
        throw new RuntimeException("User Service is currently unavailable while deleting userId: " + userId,ex);
    }
}