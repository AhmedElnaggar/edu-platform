package com.edu.course.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {

    @GetMapping("/users/profile/{userId}")
    Map<String, Object> getUserProfile(@PathVariable("userId") String userId,
                                       @RequestHeader("Authorization") String authHeader);

    @GetMapping("/users/exists/{userId}")
    Map<String, Boolean> checkUserExists(@PathVariable("userId") String userId,
                                         @RequestHeader("Authorization") String authHeader);
}