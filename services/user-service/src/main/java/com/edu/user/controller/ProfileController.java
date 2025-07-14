package com.edu.user.controller;

import com.edu.user.entity.UserPreferences;
import com.edu.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users/preferences")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<UserPreferences> getUserPreferences() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Fetching preferences for user: {}", username);

        // Extract userId from JWT token (mock for now)
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        UserPreferences preferences = profileService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @PutMapping
    public ResponseEntity<UserPreferences> updateUserPreferences(@RequestBody UserPreferences preferences) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Updating preferences for user: {}", username);

        // Extract userId from JWT token (mock for now)
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        UserPreferences updated = profileService.updateUserPreferences(userId, preferences);
        return ResponseEntity.ok(updated);
    }
}