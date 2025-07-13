package com.edu.user.service;

import com.edu.user.dto.UserDto;
import com.edu.user.entity.UserProfile;
import com.edu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserByUserId(UUID userId) {
        UserProfile profile = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(profile);
    }

    public UserDto createUser(UserDto userDto) {
        UserProfile profile = convertToEntity(userDto);
        UserProfile saved = userRepository.save(profile);
        return convertToDto(saved);
    }

    private UserDto convertToDto(UserProfile profile) {
        UserDto dto = new UserDto();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUserId());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setBio(profile.getBio());
        dto.setPhoneNumber(profile.getPhoneNumber());
        return dto;
    }

    private UserProfile convertToEntity(UserDto dto) {
        UserProfile profile = new UserProfile();
        profile.setUserId(dto.getUserId());
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setBio(dto.getBio());
        profile.setPhoneNumber(dto.getPhoneNumber());
        return profile;
    }
}