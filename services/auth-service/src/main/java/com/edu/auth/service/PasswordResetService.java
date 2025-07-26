package com.edu.auth.service;


import com.edu.auth.dto.ForgotPasswordRequest;
import com.edu.auth.dto.PasswordResetResponse;
import com.edu.auth.dto.ResetPasswordRequest;
import com.edu.auth.entity.User;

import com.edu.auth.event.PasswordResetRequestedEvent;
import com.edu.auth.event.PasswordResetSuccessEvent;
import com.edu.auth.exception.InvalidTokenException;
import com.edu.auth.exception.UserNotFoundException;
import com.edu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.password-reset.token-expiration-hours:1}")
    private int tokenExpirationHours;

    @Value("${app.password-reset.max-attempts:3}")
    private int maxResetAttempts;

    @Transactional
    public PasswordResetResponse requestPasswordReset(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Check if user account is active
        if (!user.isEnabled()) {
            throw new IllegalStateException("Cannot reset password for inactive account");
        }

        // Generate reset token
        String resetToken = generateResetToken();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(tokenExpirationHours);

        // Update user with reset token
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(expiryTime);
        userRepository.save(user);

        // Publish event to send reset email
        eventPublisher.publishEvent(new PasswordResetRequestedEvent(user, resetToken));

        log.info("Password reset token generated for user: {}", user.getId());

        return PasswordResetResponse.builder()
                .success(true)
                .message("Password reset instructions have been sent to your email address")
                .build();
    }

    @Transactional
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset with token: {}", request.getToken());

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirmation do not match");
        }

        // Find user by reset token
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        // Check token expiry
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            // Clear expired token
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);
            userRepository.save(user);
            throw new InvalidTokenException("Reset token has expired. Please request a new password reset.");
        }

        // Validate new password
        validateNewPassword(request.getNewPassword(), user);

        // Update password and clear reset token
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);

        // Force user to re-authenticate by updating credentials timestamp
        user.setCredentialsNonExpired(true);

        userRepository.save(user);

        // Publish success event
        eventPublisher.publishEvent(new PasswordResetSuccessEvent(user));

        log.info("Password reset successfully for user: {}", user.getId());

        return PasswordResetResponse.builder()
                .success(true)
                .message("Your password has been reset successfully. You can now log in with your new password.")
                .build();
    }

    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        return userRepository.findByPasswordResetToken(token)
                .map(user -> user.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        log.debug("Cleaning up expired password reset tokens");

        LocalDateTime now = LocalDateTime.now();
        userRepository.clearExpiredResetTokens(now);

        log.debug("Expired password reset tokens cleaned up");
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void validateNewPassword(String newPassword, User user) {
        // Check minimum length
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Check password complexity
        if (!newPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!newPassword.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        if (!newPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }

        if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }

        // Check if new password is different from current (optional)
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password");
        }
    }
}
