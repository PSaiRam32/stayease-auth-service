package com.stayease.auth_service.config;

import com.stayease.auth_service.dto.OwnerCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "owner-service",
        url = "${services.owner-service.url}",
        configuration = FeignClientConfig.class
)
public interface OwnerClient {

    @PostMapping("/owners/auth-internal")
    void createOwner(@RequestBody OwnerCreateRequest request);
}



