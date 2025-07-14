package com.edu.user.service;

import com.edu.user.dto.ProfileDto;
import com.edu.user.dto.UserDto;
import com.edu.user.entity.UserProfile;
import com.edu.user.entity.UserPreferences;
import com.edu.user.events.UserEventPublisher;
import com.edu.user.exception.UserNotFoundException;
import com.edu.user.exception.UserAlreadyExistsException;
import com.edu.user.repository.UserRepository;
import com.edu.user.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserEventPublisher eventPublisher;

    @Cacheable(value = "user-profiles", key = "#userId")
    public UserDto getUserProfile(UUID userId) {
        log.info("Fetching user profile for userId: {}", userId);

        UserProfile profile = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User profile not found for userId: " + userId));

        return convertToDto(profile);
    }

    @Cacheable(value = "user-profiles", key = "#id")
    public UserDto getUserById(UUID id) {
        log.info("Fetching user by id: {}", id);

        UserProfile profile = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return convertToDto(profile);
    }

    @Cacheable(value = "user-search", key = "#searchTerm + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        log.info("Searching users with term: {}", searchTerm);

        Page<UserProfile> profiles = userRepository.searchUsers(searchTerm, pageable);
        return profiles.map(this::convertToDto);
    }

    public Page<UserDto> getPublicProfiles(Pageable pageable) {
        log.info("Fetching public user profiles");

        Page<UserProfile> profiles = userRepository.findPublicProfiles(pageable);
        return profiles.map(this::convertToDto);
    }

    @Transactional
    @CacheEvict(value = "user-profiles", key = "#userId")
    public UserDto createUserProfile(UUID userId, ProfileDto profileDto) {
        log.info("Creating user profile for userId: {}", userId);

        if (userRepository.existsByUserId(userId)) {
            throw new UserAlreadyExistsException("User profile already exists for userId: " + userId);
        }

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .firstName(profileDto.getFirstName())
                .lastName(profileDto.getLastName())
                .displayName(profileDto.getDisplayName())
                .bio(profileDto.getBio())
                .profilePictureUrl(profileDto.getProfilePictureUrl())
                .phoneNumber(profileDto.getPhoneNumber())
                .dateOfBirth(profileDto.getDateOfBirth())
                .gender(profileDto.getGender())
                .location(profileDto.getLocation())
                .timezone(profileDto.getTimezone())
                .language(profileDto.getLanguage())
                .websiteUrl(profileDto.getWebsiteUrl())
                .linkedinUrl(profileDto.getLinkedinUrl())
                .twitterUrl(profileDto.getTwitterUrl())
                .profileVisibility(profileDto.getProfileVisibility())
                .emailNotifications(profileDto.getEmailNotifications())
                .pushNotifications(profileDto.getPushNotifications())
                .build();

        profile = userRepository.save(profile);

        // Create default preferences
        createDefaultPreferences(userId);

        // Publish event
        eventPublisher.publishUserProfileCreated(profile);

        log.info("User profile created successfully for userId: {}", userId);
        return convertToDto(profile);
    }

    @Transactional
    @CacheEvict(value = {"user-profiles", "user-search"}, key = "#userId")
    public UserDto updateUserProfile(UUID userId, ProfileDto profileDto) {
        log.info("Updating user profile for userId: {}", userId);

        UserProfile profile = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User profile not found for userId: " + userId));

        // Update fields
        updateProfileFields(profile, profileDto);
        profile = userRepository.save(profile);

        // Publish event
        eventPublisher.publishUserProfileUpdated(profile);

        log.info("User profile updated successfully for userId: {}", userId);
        return convertToDto(profile);
    }

    @Transactional
    @CacheEvict(value = {"user-profiles", "user-search"}, key = "#userId")
    public void deleteUserProfile(UUID userId) {
        log.info("Deleting user profile for userId: {}", userId);

        UserProfile profile = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User profile not found for userId: " + userId));

        // Delete preferences first
        userPreferencesRepository.deleteByUserId(userId);

        // Delete profile
        userRepository.delete(profile);

        // Publish event
        eventPublisher.publishUserProfileDeleted(userId);

        log.info("User profile deleted successfully for userId: {}", userId);
    }

    public List<UserDto> getUsersByLocation(String location) {
        log.info("Fetching users by location: {}", location);

        List<UserProfile> profiles = userRepository.findByLocationContainingIgnoreCase(location);
        return profiles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean existsByUserId(UUID userId) {
        return userRepository.existsByUserId(userId);
    }

    public long getTodaysRegistrations() {
        return userRepository.countTodaysRegistrations();
    }

    private void createDefaultPreferences(UUID userId) {
        UserPreferences preferences = UserPreferences.builder()
                .userId(userId)
                .build(); // Uses default values from @Builder.Default

        userPreferencesRepository.save(preferences);
    }

    private void updateProfileFields(UserProfile profile, ProfileDto dto) {
        Optional.ofNullable(dto.getFirstName()).ifPresent(profile::setFirstName);
        Optional.ofNullable(dto.getLastName()).ifPresent(profile::setLastName);
        Optional.ofNullable(dto.getDisplayName()).ifPresent(profile::setDisplayName);
        Optional.ofNullable(dto.getBio()).ifPresent(profile::setBio);
        Optional.ofNullable(dto.getProfilePictureUrl()).ifPresent(profile::setProfilePictureUrl);
        Optional.ofNullable(dto.getPhoneNumber()).ifPresent(profile::setPhoneNumber);
        Optional.ofNullable(dto.getDateOfBirth()).ifPresent(profile::setDateOfBirth);
        Optional.ofNullable(dto.getGender()).ifPresent(profile::setGender);
        Optional.ofNullable(dto.getLocation()).ifPresent(profile::setLocation);
        Optional.ofNullable(dto.getTimezone()).ifPresent(profile::setTimezone);
        Optional.ofNullable(dto.getLanguage()).ifPresent(profile::setLanguage);
        Optional.ofNullable(dto.getWebsiteUrl()).ifPresent(profile::setWebsiteUrl);
        Optional.ofNullable(dto.getLinkedinUrl()).ifPresent(profile::setLinkedinUrl);
        Optional.ofNullable(dto.getTwitterUrl()).ifPresent(profile::setTwitterUrl);
        Optional.ofNullable(dto.getProfileVisibility()).ifPresent(profile::setProfileVisibility);
        Optional.ofNullable(dto.getEmailNotifications()).ifPresent(profile::setEmailNotifications);
        Optional.ofNullable(dto.getPushNotifications()).ifPresent(profile::setPushNotifications);
    }

    private UserDto convertToDto(UserProfile profile) {
        return UserDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .displayName(profile.getDisplayName())
                .bio(profile.getBio())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .phoneNumber(profile.getPhoneNumber())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .location(profile.getLocation())
                .timezone(profile.getTimezone())
                .language(profile.getLanguage())
                .websiteUrl(profile.getWebsiteUrl())
                .linkedinUrl(profile.getLinkedinUrl())
                .twitterUrl(profile.getTwitterUrl())
                .profileVisibility(profile.getProfileVisibility())
                .emailNotifications(profile.getEmailNotifications())
                .pushNotifications(profile.getPushNotifications())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}