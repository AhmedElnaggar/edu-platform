package com.edu.auth.controller;

import com.edu.auth.dto.LoginRequest;
import com.edu.auth.dto.LoginResponse;
import com.edu.auth.dto.RegisterRequest;
import com.edu.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        LoginResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());
        LoginResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                String username = authService.getUsernameFromToken(token);
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "username", username
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (authService.validateToken(refreshToken)) {
            String username = authService.getUsernameFromToken(refreshToken);
            // For now, return the same token. In production, implement proper refresh logic
            return ResponseEntity.ok(Map.of(
                    "message", "Token refresh endpoint - implement refresh logic",
                    "username", username
            ));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Invalid refresh token"));
    }
}