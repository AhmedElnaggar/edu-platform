package com.edu.user.controller;

import com.edu.user.dto.ProfileDto;
import com.edu.user.dto.UserDto;
import com.edu.user.service.UserService;
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
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Fetching current user profile for: {}", username);

        // For now, we'll use a mock userId. In real implementation,
        // you'd extract userId from JWT token
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        UserDto userDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
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

    @PostMapping("/profile")
    public ResponseEntity<UserDto> createUserProfile(@Valid @RequestBody ProfileDto profileDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Creating user profile for: {}", username);

        // Extract userId from JWT token (mock for now)
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        UserDto userDto = userService.createUserProfile(userId, profileDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateUserProfile(@Valid @RequestBody ProfileDto profileDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Updating user profile for: {}", username);

        // Extract userId from JWT token (mock for now)
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        UserDto userDto = userService.updateUserProfile(userId, profileDto);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Map<String, String>> deleteUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Deleting user profile for: {}", username);

        // Extract userId from JWT token (mock for now)
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

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