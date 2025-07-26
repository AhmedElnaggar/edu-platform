package com.edu.auth.service;

import com.edu.auth.event.VerificationEmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRetryService {

    private final EmailService emailService;
    private final ConcurrentMap<String, EmailRetryInfo> retryQueue = new ConcurrentHashMap<>();

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MINUTES = 5;

    public void scheduleEmailRetry(VerificationEmailEvent event) {
        String retryKey = event.getEventId();

        EmailRetryInfo retryInfo = retryQueue.computeIfAbsent(retryKey, k ->
                new EmailRetryInfo(event, 0, LocalDateTime.now().plusMinutes(RETRY_DELAY_MINUTES)));

        retryInfo.incrementAttempts();
        retryInfo.setNextRetryTime(LocalDateTime.now().plusMinutes(RETRY_DELAY_MINUTES * retryInfo.getAttempts()));

        log.info("Scheduled email retry {} for user: {} at {}",
                retryInfo.getAttempts(), event.getUser().getId(), retryInfo.getNextRetryTime());
    }

    @Scheduled(fixedDelay = 60000) // Run every minute
    @Async("emailExecutor")
    public void processRetryQueue() {
        LocalDateTime now = LocalDateTime.now();

        retryQueue.entrySet().removeIf(entry -> {
            EmailRetryInfo retryInfo = entry.getValue();

            if (retryInfo.getAttempts() >= MAX_RETRY_ATTEMPTS) {
                log.error("Max retry attempts reached for email: {}, giving up", entry.getKey());
                return true; // Remove from queue
            }

            if (retryInfo.getNextRetryTime().isBefore(now)) {
                log.info("Retrying email send attempt {} for user: {}",
                        retryInfo.getAttempts(), retryInfo.getEmailEvent().getUser().getId());

                CompletableFuture<Void> retryFuture = emailService.sendVerificationEmailAsync(
                        retryInfo.getEmailEvent().getUser());

                retryFuture.whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Email retry failed for user: {}",
                                retryInfo.getEmailEvent().getUser().getId(), throwable);
                        scheduleEmailRetry(retryInfo.getEmailEvent());
                    } else {
                        log.info("Email retry successful for user: {}",
                                retryInfo.getEmailEvent().getUser().getId());
                        retryQueue.remove(entry.getKey()); // Success, remove from queue
                    }
                });

                return false; // Keep in queue until completion
            }

            return false; // Not time yet, keep in queue
        });
    }

    private static class EmailRetryInfo {
        private final VerificationEmailEvent emailEvent;
        private int attempts;
        private LocalDateTime nextRetryTime;

        public EmailRetryInfo(VerificationEmailEvent emailEvent, int attempts, LocalDateTime nextRetryTime) {
            this.emailEvent = emailEvent;
            this.attempts = attempts;
            this.nextRetryTime = nextRetryTime;
        }

        public VerificationEmailEvent getEmailEvent() { return emailEvent; }
        public int getAttempts() { return attempts; }
        public void incrementAttempts() { this.attempts++; }
        public LocalDateTime getNextRetryTime() { return nextRetryTime; }
        public void setNextRetryTime(LocalDateTime nextRetryTime) { this.nextRetryTime = nextRetryTime; }
    }
}
