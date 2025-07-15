package com.edu.gateway.controller;

import com.edu.gateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private JwtService jwtService;

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API Gateway is working!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("valid", false);
                response.put("error", "Invalid Authorization header format");
                return ResponseEntity.badRequest().body(response);
            }

            String token = authHeader.substring(7);

            // Check JWT format
            if (!jwtService.isValidJwtFormat(token)) {
                response.put("valid", false);
                response.put("error", "Invalid JWT format");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate token
            boolean isValid = !jwtService.isInvalid(token);
            response.put("valid", isValid);

            if (isValid) {
                response.put("userId", jwtService.extractUserId(token));
                response.put("username", jwtService.extractUsername(token));
                response.put("role", jwtService.extractUserRole(token));
                response.put("expiration", jwtService.extractExpiration(token));
            } else {
                response.put("error", "Token is invalid or expired");
            }

        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", "Token validation failed: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/headers")
    public ResponseEntity<?> checkHeaders(@RequestHeader Map<String, String> headers) {
        Map<String, Object> response = new HashMap<>();
        response.put("headers", headers);
        response.put("hasAuthorization", headers.containsKey("authorization"));

        if (headers.containsKey("authorization")) {
            String authHeader = headers.get("authorization");
            response.put("authHeaderFormat", authHeader.startsWith("Bearer ") ? "correct" : "incorrect");
        }

        return ResponseEntity.ok(response);
    }
}