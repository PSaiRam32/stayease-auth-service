package com.stayease.auth_service.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignClientConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            log.debug("Processing Feign request interception");
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                String correlationId = request.getHeader("X-Correlation-Id");
                
                if (authHeader != null) {
                    log.debug("Adding Authorization header to Feign request");
                    requestTemplate.header("Authorization", authHeader);
                }
                if (correlationId != null) {
                    log.debug("Adding X-Correlation-Id header to Feign request: {}", correlationId);
                    requestTemplate.header("X-Correlation-Id", correlationId);
                }
            } else {
                log.debug("No request attributes found for Feign request interception");
            }
        };
    }
}