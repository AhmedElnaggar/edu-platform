package com.edu.auth.service;

import com.edu.auth.event.EmailEvent;
import com.edu.auth.event.UserRegisteredEvent;
import com.edu.auth.event.VerificationEmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueService {

    private final EmailService emailService;
    private final EmailRetryService emailRetryService;

    @EventListener
    @Async("eventExecutor")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling user registration event for user: {}", event.getUser().getUsername());

        try {
            // Create verification email event
            VerificationEmailEvent emailEvent = new VerificationEmailEvent(event.getUser());

            // Process email event
            handleVerificationEmail(emailEvent);

        } catch (Exception e) {
            log.error("Error handling user registration event for user: {}",
                    event.getUser().getId(), e);
        }
    }

    @EventListener
    @Async("emailExecutor")
    public void handleVerificationEmail(VerificationEmailEvent event) {
        log.info("Processing verification email for: {}", event.getRecipient());

        CompletableFuture<Void> emailFuture = emailService.sendVerificationEmailAsync(event.getUser());

        emailFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("Email sending failed for user: {}, scheduling retry",
                        event.getUser().getId(), throwable);
                emailRetryService.scheduleEmailRetry(event);
            } else {
                log.info("Verification email sent successfully to: {}", event.getRecipient());
            }
        });
    }

    @EventListener
    @Async("emailExecutor")
    public void handleGenericEmailEvent(EmailEvent event) {
        log.info("Processing generic email event: {} for recipient: {}",
                event.getEmailType(), event.getRecipient());

        // Handle other email types here
        switch (event.getEmailType()) {
            case "PASSWORD_RESET":
                // Handle password reset email
                break;
            case "WELCOME":
                // Handle welcome email
                break;
            default:
                log.warn("Unknown email type: {}", event.getEmailType());
        }
    }
}