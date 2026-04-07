package com.stayease.auth_service.config;


import com.stayease.auth_service.dto.UserProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}",
        configuration = FeignClientConfig.class
)
public interface UserClientConfig {

    @PostMapping("/users/auth-internal")
    void createUser(@RequestBody UserProfileRequest request);

}

