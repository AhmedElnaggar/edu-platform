package com.edu.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    // Notification Preferences
    @Builder.Default
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Builder.Default
    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;

    @Builder.Default
    @Column(name = "sms_notifications")
    private Boolean smsNotifications = false;

    @Builder.Default
    @Column(name = "marketing_emails")
    private Boolean marketingEmails = false;

    // Course Notifications
    @Builder.Default
    @Column(name = "course_reminders")
    private Boolean courseReminders = true;

    @Builder.Default
    @Column(name = "assignment_reminders")
    private Boolean assignmentReminders = true;

    @Builder.Default
    @Column(name = "deadline_notifications")
    private Boolean deadlineNotifications = true;

    // UI Preferences
    @Builder.Default
    @Column(length = 20)
    private String theme = "light";

    @Builder.Default
    @Column(length = 10)
    private String language = "en";

    @Builder.Default
    @Column(name = "items_per_page")
    private Integer itemsPerPage = 20;

    // Privacy Preferences
    @Builder.Default
    @Column(name = "profile_public")
    private Boolean profilePublic = true;

    @Builder.Default
    @Column(name = "show_online_status")
    private Boolean showOnlineStatus = true;

    @Builder.Default
    @Column(name = "allow_messages")
    private Boolean allowMessages = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}