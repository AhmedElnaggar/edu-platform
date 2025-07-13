package com.edu.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String bio;
    private String phoneNumber;
}