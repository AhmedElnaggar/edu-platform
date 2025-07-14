package com.edu.user.events;

import com.edu.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_PROFILE_CREATED_TOPIC = "user.profile.created";
    private static final String USER_PROFILE_UPDATED_TOPIC = "user.profile.updated";
    private static final String USER_PROFILE_DELETED_TOPIC = "user.profile.deleted";

    public void publishUserProfileCreated(UserProfile profile) {
        try {
            Map<String, Object> event = createBaseEvent(profile);
            event.put("eventType", "PROFILE_CREATED");

            kafkaTemplate.send(USER_PROFILE_CREATED_TOPIC, profile.getUserId().toString(), event);
            log.info("Published user profile created event for userId: {}", profile.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish user profile created event for userId: {}", profile.getUserId(), e);
        }
    }

    public void publishUserProfileUpdated(UserProfile profile) {
        try {
            Map<String, Object> event = createBaseEvent(profile);
            event.put("eventType", "PROFILE_UPDATED");

            kafkaTemplate.send(USER_PROFILE_UPDATED_TOPIC, profile.getUserId().toString(), event);
            log.info("Published user profile updated event for userId: {}", profile.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish user profile updated event for userId: {}", profile.getUserId(), e);
        }
    }

    public void publishUserProfileDeleted(UUID userId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "PROFILE_DELETED");
            event.put("userId", userId.toString());
            event.put("timestamp", LocalDateTime.now());
            event.put("source", "user-service");

            kafkaTemplate.send(USER_PROFILE_DELETED_TOPIC, userId.toString(), event);
            log.info("Published user profile deleted event for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish user profile deleted event for userId: {}", userId, e);
        }
    }

    private Map<String, Object> createBaseEvent(UserProfile profile) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", profile.getUserId().toString());
        event.put("profileId", profile.getId().toString());
        event.put("firstName", profile.getFirstName());
        event.put("lastName", profile.getLastName());
        event.put("displayName", profile.getDisplayName());
        event.put("email", null); // We don't have email in user service
        event.put("location", profile.getLocation());
        event.put("profileVisibility", profile.getProfileVisibility());
        event.put("timestamp", LocalDateTime.now());
        event.put("source", "user-service");
        event.put("version", "1.0");

        return event;
    }
}