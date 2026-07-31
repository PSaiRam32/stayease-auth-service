package com.stayease.auth_service.integration;

import com.stayease.auth_service.config.OwnerClient;
import com.stayease.auth_service.dto.Request.OwnerCreateRequest;
import com.stayease.auth_service.dto.Request.UserVerificationRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerServiceGateway {

    private final OwnerClient ownerClient;

    @Retry(name="owner-service",fallbackMethod = "createOwnerFallback")
    @CircuitBreaker(name="owner-service")
    public void createOwner(OwnerCreateRequest request){
        ownerClient.createOwner(request);
    }

    public void createOwnerFallback(OwnerCreateRequest request, Exception ex){
        log.error("Owner Service unavailable", ex);
        throw new RuntimeException("Owner Service is temporarily unavailable.Please Try again Later",ex);
    }

    @Retry(name = "owner-service",fallbackMethod = "verifyOwnerFallback")
    @CircuitBreaker(name = "owner-service")
    public void verifyOwner(Long ownerId,UserVerificationRequest request){
        ownerClient.verifyOwner(ownerId, request);
    }

    public void verifyOwnerFallback(Long ownerId,UserVerificationRequest request,Exception ex){
        log.error("Verification failed",ex);
        throw new RuntimeException("Unable to verify user currently.",ex);
    }
}
