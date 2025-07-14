package com.edu.user.service;

import com.edu.user.entity.UserPreferences;
import com.edu.user.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserPreferencesRepository userPreferencesRepository;

    @Cacheable(value = "user-preferences", key = "#userId")
    public UserPreferences getUserPreferences(UUID userId) {
        log.info("Fetching preferences for userId: {}", userId);

        return userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    @Transactional
    @CacheEvict(value = "user-preferences", key = "#userId")
    public UserPreferences updateUserPreferences(UUID userId, UserPreferences preferences) {
        log.info("Updating preferences for userId: {}", userId);

        UserPreferences existing = userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        // Update fields
        existing.setEmailNotifications(preferences.getEmailNotifications());
        existing.setPushNotifications(preferences.getPushNotifications());
        existing.setSmsNotifications(preferences.getSmsNotifications());
        existing.setMarketingEmails(preferences.getMarketingEmails());
        existing.setCourseReminders(preferences.getCourseReminders());
        existing.setAssignmentReminders(preferences.getAssignmentReminders());
        existing.setDeadlineNotifications(preferences.getDeadlineNotifications());
        existing.setTheme(preferences.getTheme());
        existing.setLanguage(preferences.getLanguage());
        existing.setItemsPerPage(preferences.getItemsPerPage());
        existing.setProfilePublic(preferences.getProfilePublic());
        existing.setShowOnlineStatus(preferences.getShowOnlineStatus());
        existing.setAllowMessages(preferences.getAllowMessages());

        return userPreferencesRepository.save(existing);
    }

    private UserPreferences createDefaultPreferences(UUID userId) {
        UserPreferences preferences = UserPreferences.builder()
                .userId(userId)
                .build();

        return userPreferencesRepository.save(preferences);
    }
}