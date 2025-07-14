package com.edu.course.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDto {
    private String id;
    private String courseId;
    private String userId;
    private String status;
    private Double progress;
    private Double amountPaid;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;

    // Course info
    private String courseTitle;
    private String courseInstructor;
    private String courseThumbnail;

    // User info
    private String userName;
    private String userEmail;
}