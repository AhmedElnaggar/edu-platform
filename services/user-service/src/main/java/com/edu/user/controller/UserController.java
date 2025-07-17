package com.edu.user.controller;

import com.edu.user.dto.ProfileDto;
import com.edu.user.dto.UserDto;
import com.edu.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUserProfile(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");

        log.info("Fetching current user profile for: {} (ID: {})", username, userIdHeader);

        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing X-User-Id header from API Gateway");
        }

        UUID userId = UUID.fromString(userIdHeader);
        UserDto userDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") UUID id) {
        log.info("Fetching user by id: {}", id);
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable("userId") UUID userId) {
        log.info("Fetching user profile for userId: {}", userId);
        UserDto userDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(userDto);
    }

    // ✅ FIXED: Create Profile with Headers from Gateway
    @PostMapping("/profile")
    public ResponseEntity<UserDto> createUserProfile(
            @Valid @RequestBody ProfileDto profileDto,
            HttpServletRequest request) {

        // ✅ Extract user info from API Gateway headers
        String userIdHeader = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String userRole = request.getHeader("X-User-Role");

        log.info("Creating user profile for userId: {}, username: {}, role: {}",
                userIdHeader, username, userRole);

        // Validate headers
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing X-User-Id header from API Gateway");
        }

        UUID userId = UUID.fromString(userIdHeader);

        // Create profile
        UserDto userDto = userService.createUserProfile(userId, profileDto);

        log.info("✅ User profile created successfully for userId: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    // ✅ FIXED: Update Profile with Headers
    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateUserProfile(
            @Valid @RequestBody ProfileDto profileDto,
            HttpServletRequest request) {

        String userIdHeader = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");

        log.info("Updating user profile for userId: {}, username: {}", userIdHeader, username);

        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing X-User-Id header from API Gateway");
        }

        UUID userId = UUID.fromString(userIdHeader);
        UserDto userDto = userService.updateUserProfile(userId, profileDto);
        return ResponseEntity.ok(userDto);
    }

    // ✅ FIXED: Delete Profile with Headers
    @DeleteMapping("/profile")
    public ResponseEntity<Map<String, String>> deleteUserProfile(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");

        log.info("Deleting user profile for userId: {}, username: {}", userIdHeader, username);

        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing X-User-Id header from API Gateway");
        }

        UUID userId = UUID.fromString(userIdHeader);
        userService.deleteUserProfile(userId);
        return ResponseEntity.ok(Map.of("message", "User profile deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDto>> searchUsers(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Searching users with query: {}", q);
        Page<UserDto> users = userService.searchUsers(q, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<UserDto>> getPublicProfiles(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Fetching public user profiles");
        Page<UserDto> users = userService.getPublicProfiles(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<UserDto>> getUsersByLocation(@PathVariable String location) {
        log.info("Fetching users by location: {}", location);
        List<UserDto> users = userService.getUsersByLocation(location);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/exists/{userId}")
    public ResponseEntity<Map<String, Boolean>> checkUserExists(@PathVariable UUID userId) {
        boolean exists = userService.existsByUserId(userId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/stats/today")
    public ResponseEntity<Map<String, Long>> getTodaysRegistrations() {
        long count = userService.getTodaysRegistrations();
        return ResponseEntity.ok(Map.of("todaysRegistrations", count));
    }
}