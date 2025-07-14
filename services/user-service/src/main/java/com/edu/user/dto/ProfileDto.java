package com.edu.user.dto;

import com.edu.user.entity.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Size(max = 150, message = "Display name must not exceed 150 characters")
    private String displayName;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    private String profilePictureUrl;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    private LocalDate dateOfBirth;

    private UserProfile.Gender gender;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    private String timezone;
    private String language;
    private String websiteUrl;
    private String linkedinUrl;
    private String twitterUrl;
    private UserProfile.ProfileVisibility profileVisibility;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
}