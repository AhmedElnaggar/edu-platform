package com.edu.auth;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        // Implementation here
        return "mock-jwt-token";
    }
}