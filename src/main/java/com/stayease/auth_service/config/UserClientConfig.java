package com.stayease.auth_service.config;


import com.stayease.auth_service.dto.Request.UserProfileRequest;
import com.stayease.auth_service.dto.Request.UserVerificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}",
        configuration = FeignClientConfig.class
)
public interface UserClientConfig {

    @PostMapping("/users/auth-internal")
    void createUser(@RequestBody UserProfileRequest request);

    @DeleteMapping("/users/delete-internal/{userId}")
    void deleteUser(@PathVariable("userId") Long userId);

    @PutMapping("/users/auth-internal/verify/{userId}")
    void verifyUser(@PathVariable Long userId,@RequestBody UserVerificationRequest request);

}

