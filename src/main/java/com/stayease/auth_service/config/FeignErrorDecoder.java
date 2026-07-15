//package com.stayease.auth_service.config;
//
//import feign.Response;
//import feign.codec.ErrorDecoder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class FeignErrorDecoder implements ErrorDecoder {
//
//    @Override
//    public Exception decode(String methodKey, Response response) {
//        int status = response.status();
//        return switch (status) {
//            case 400 -> new RuntimeException("Bad Request from downstream service");
//            case 404 -> new RuntimeException("Resource not found in downstream service");
//            case 500 -> new RuntimeException("Internal server error in downstream service");
//            default -> new RuntimeException("Feign client error: " + status);
//        };
//    }
//}