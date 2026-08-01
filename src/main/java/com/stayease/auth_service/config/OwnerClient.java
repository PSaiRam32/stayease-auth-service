package com.stayease.auth_service.config;

import com.stayease.auth_service.dto.Request.OwnerCreateRequest;
import com.stayease.auth_service.dto.Request.UserVerificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "owner-service",configuration = FeignConfig.class)
public interface OwnerClient{

    @PostMapping("/owners/auth-internal")
    void createOwner(@RequestBody OwnerCreateRequest request);

    @PutMapping("/owners/auth-internal/verify/{ownerId}")
    void verifyOwner(@PathVariable Long ownerId,@RequestBody UserVerificationRequest request);
}



