package com.edu.user.dto;

import com.edu.user.entity.UserProfile;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String bio;
    private String profilePictureUrl;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private UserProfile.Gender gender;
    private String location;
    private String timezone;
    private String language;
    private String websiteUrl;
    private String linkedinUrl;
    private String twitterUrl;
    private UserProfile.ProfileVisibility profileVisibility;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}