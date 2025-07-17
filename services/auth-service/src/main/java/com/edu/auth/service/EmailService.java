package com.edu.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import com.edu.auth.entity.User;

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

    @Async
    public void sendVerificationEmail(User user) {
        try {
            log.info("Sending verification email to: {}", user.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "EDU Platform");
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to EDU Platform - Verify Your Email");

            String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + user.getEmailVerificationToken();

            Context context = new Context();
            context.setVariable("firstName", extractFirstNameFromEmail(user.getEmail()));
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

    private String extractFirstNameFromEmail(String email) {
        // Simple extraction - in real implementation, this would come from user profile
        return email.split("@")[0].substring(0, 1).toUpperCase() +
                email.split("@")[0].substring(1);
    }
}
