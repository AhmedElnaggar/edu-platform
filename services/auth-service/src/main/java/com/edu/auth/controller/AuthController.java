package com.edu.auth.controller;

import com.edu.auth.dto.*;
import com.edu.auth.exception.InvalidTokenException;
import com.edu.auth.exception.UserNotFoundException;
import com.edu.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

    @GetMapping("/verify-email")
    public ResponseEntity<LoginResponse> verifyEmail(@RequestParam("token") String token) {
        LoginResponse response = authService.verifyEmail(token);
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

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            PasswordResetResponse response = authService.requestPasswordReset(request);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            // Return success message to prevent email enumeration
            return ResponseEntity.ok(PasswordResetResponse.builder()
                    .success(true)
                    .message("If an account with that email exists, password reset instructions have been sent")
                    .build());
        } catch (Exception e) {
            log.error("Error processing password reset request", e);
            return ResponseEntity.badRequest()
                    .body(PasswordResetResponse.builder()
                            .success(false)
                            .message("An error occurred while processing your request")
                            .build());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            PasswordResetResponse response = authService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (InvalidTokenException | IllegalArgumentException e) {
            log.warn("Invalid password reset attempt: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(PasswordResetResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error processing password reset", e);
            return ResponseEntity.badRequest()
                    .body(PasswordResetResponse.builder()
                            .success(false)
                            .message("An error occurred while resetting your password")
                            .build());
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam String token) {
        boolean isValid = authService.validateResetToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> showResetPasswordForm(@RequestParam("token") String token) {
        if (!authService.validateResetToken(token)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body("""
                    <html>
                    <head><title>Invalid Reset Link</title></head>
                    <body>
                        <h2>Invalid or Expired Reset Link</h2>
                        <p>This password reset link is invalid or has expired.</p>
                        <p><a href="/auth/forgot-password">Request a new password reset</a></p>
                    </body>
                    </html>
                    """);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(generateResetPasswordForm(token));
    }

    private String generateResetPasswordForm(String token) {
        return String.format("""
        <html>
        <head>
            <title>Reset Your Password</title>
            <style>
                body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; }
                .form-group { margin-bottom: 15px; }
                label { display: block; margin-bottom: 5px; font-weight: bold; }
                input[type="password"] { width: 100%%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
                button { background-color: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; width: 100%%; }
                button:hover { background-color: #0056b3; }
                .error { color: red; margin-top: 5px; }
            </style>
        </head>
        <body>
            <h2>Reset Your Password</h2>
            <form id="resetForm" onsubmit="submitReset(event)">
                <input type="hidden" name="token" value="%s">
                
                <div class="form-group">
                    <label for="newPassword">New Password:</label>
                    <input type="password" id="newPassword" name="newPassword" required 
                           minlength="8" placeholder="Enter your new password">
                    <div class="error" id="passwordError"></div>
                </div>
                
                <div class="form-group">
                    <label for="confirmPassword">Confirm Password:</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required 
                           placeholder="Confirm your new password">
                    <div class="error" id="confirmError"></div>
                </div>
                
                <button type="submit">Reset Password</button>
            </form>
            
            <div id="message" style="margin-top: 20px;"></div>
            
            <script>
                function submitReset(event) {
                    event.preventDefault();
                    
                    const formData = new FormData(event.target);
                    const data = {
                        token: formData.get('token'),
                        newPassword: formData.get('newPassword'),
                        confirmPassword: formData.get('confirmPassword')
                    };
                    
                    // Clear previous errors
                    document.getElementById('passwordError').textContent = '';
                    document.getElementById('confirmError').textContent = '';
                    document.getElementById('message').textContent = '';
                    
                    // Validate passwords match
                    if (data.newPassword !== data.confirmPassword) {
                        document.getElementById('confirmError').textContent = 'Passwords do not match';
                        return;
                    }
                    
                    // Submit to API
                    fetch('/auth/reset-password', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(data)
                    })
                    .then(response => response.json())
                    .then(result => {
                        if (result.success) {
                            document.getElementById('message').innerHTML = 
                                '<div style="color: green; padding: 15px; background-color: #d4edda; border: 1px solid #c3e6cb; border-radius: 4px;">' +
                                result.message + '<br><br>' +
                                '<a href="/auth/login">Click here to log in</a></div>';
                            document.getElementById('resetForm').style.display = 'none';
                        } else {
                            document.getElementById('message').innerHTML = 
                                '<div style="color: red; padding: 15px; background-color: #f8d7da; border: 1px solid #f5c6cb; border-radius: 4px;">' +
                                result.message + '</div>';
                        }
                    })
                    .catch(error => {
                        document.getElementById('message').innerHTML = 
                            '<div style="color: red;">An error occurred. Please try again.</div>';
                    });
                }
            </script>
        </body>
        </html>
        """, token);
    }
}