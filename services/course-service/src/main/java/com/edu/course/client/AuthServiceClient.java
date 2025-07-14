package com.edu.course.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "auth-service", url = "${auth-service.url}")
public interface AuthServiceClient {

    @PostMapping("/auth/validate")
    Map<String, Object> validateToken(@RequestHeader("Authorization") String authHeader);
}