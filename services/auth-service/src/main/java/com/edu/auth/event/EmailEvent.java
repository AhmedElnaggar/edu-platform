package com.edu.auth.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public abstract class EmailEvent extends ApplicationEvent {
    private final String recipient;
    private final String emailType;
    private final Map<String, Object> templateData;
    private final LocalDateTime createdAt;
    private final String eventId;

    public EmailEvent(Object source, String recipient, String emailType, Map<String, Object> templateData) {
        super(source);
        this.recipient = recipient;
        this.emailType = emailType;
        this.templateData = templateData;
        this.createdAt = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
}