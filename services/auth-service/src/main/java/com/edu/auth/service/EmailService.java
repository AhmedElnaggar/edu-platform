package com.edu.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import com.edu.auth.entity.User;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    // Keep your existing method for backward compatibility
    @Async
    public void sendVerificationEmail(User user) {
        if (!emailEnabled) {
            log.info("Email sending is disabled, skipping verification email for: {}", user.getEmail());
            return;
        }

        try {
            log.info("Sending verification email to: {}", user.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EDU Platform");
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to EDU Platform - Verify Your Email");

            String verificationUrl = baseUrl + "/auth/verify-email?token=" + user.getEmailVerificationToken();

            Context context = new Context();
            context.setVariable("firstName", extractFirstNameFromUser(user));
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("email/email-verification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }

    // New async method that returns CompletableFuture for the queue system
    @Async("emailExecutor")
    @Retryable(
            value = {Exception.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Void> sendVerificationEmailAsync(User user) {
        if (!emailEnabled) {
            log.info("Email sending is disabled, skipping verification email for: {}", user.getEmail());
            return CompletableFuture.completedFuture(null);
        }

        try {
            log.info("Sending verification email async to: {}", user.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EDU Platform");
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to EDU Platform - Verify Your Email");

            String verificationUrl = baseUrl + "/auth/verify-email?token=" + user.getEmailVerificationToken();

            Context context = new Context();
            context.setVariable("firstName", extractFirstNameFromUser(user));
            context.setVariable("lastName", user.getLastName());
            context.setVariable("username", user.getUsername());
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("email/email-verification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent successfully async to: {}", user.getEmail());

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to send verification email async to: {}", user.getEmail(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    // Template-based email sending for different email types
    @Async("emailExecutor")
    public CompletableFuture<Void> sendTemplateEmailAsync(String recipientEmail, String templateName,
                                                          String subject, Context templateContext) {
        if (!emailEnabled) {
            log.info("Email sending is disabled, skipping template email to: {}", recipientEmail);
            return CompletableFuture.completedFuture(null);
        }

        try {
            log.info("Sending template email '{}' to: {}", templateName, recipientEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EDU Platform");
            helper.setTo(recipientEmail);
            helper.setSubject(subject);

            String htmlContent = templateEngine.process(templateName, templateContext);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Template email '{}' sent successfully to: {}", templateName, recipientEmail);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to send template email '{}' to: {}", templateName, recipientEmail, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    // Password reset email using template
    @Async("emailExecutor")
    public CompletableFuture<Void> sendPasswordResetEmailAsync(User user, String resetToken) {
        try {
            String resetUrl = baseUrl + "/auth/reset-password?token=" + resetToken;

            Context context = new Context();
            context.setVariable("firstName", extractFirstNameFromUser(user));
            context.setVariable("lastName", user.getLastName());
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("baseUrl", baseUrl);

            return sendTemplateEmailAsync(
                    user.getEmail(),
                    "email/password-reset",
                    "Reset Your Password - EDU Platform",
                    context
            );
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    // Welcome email using template
    @Async("emailExecutor")
    public CompletableFuture<Void> sendWelcomeEmailAsync(User user) {
        try {
            Context context = new Context();
            context.setVariable("firstName", extractFirstNameFromUser(user));
            context.setVariable("lastName", user.getLastName());
            context.setVariable("username", user.getUsername());
            context.setVariable("baseUrl", baseUrl);

            return sendTemplateEmailAsync(
                    user.getEmail(),
                    "email/welcome",
                    "Welcome to EDU Platform!",
                    context
            );
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    // Enhanced name extraction
    private String extractFirstNameFromUser(User user) {
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            return user.getFirstName();
        }

        // Fallback to email-based extraction
        return extractFirstNameFromEmail(user.getEmail());
    }

    private String extractFirstNameFromEmail(String email) {
        // Simple extraction - in real implementation, this would come from user profile
        String localPart = email.split("@")[0];
        if (localPart.length() > 0) {
            return localPart.substring(0, 1).toUpperCase() +
                    (localPart.length() > 1 ? localPart.substring(1).toLowerCase() : "");
        }
        return "User";
    }
}