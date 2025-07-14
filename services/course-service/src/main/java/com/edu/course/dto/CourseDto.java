package com.edu.course.dto;

import com.edu.course.document.Course;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private String id;
    private String title;
    private String description;
    private String shortDescription;
    private String instructorId;
    private String instructorName; // Populated from User Service
    private String category;
    private String difficulty;
    private Double price;
    private String currency;
    private Double discountPrice;
    private LocalDateTime discountExpiry;
    private Integer duration;
    private Integer maxStudents;
    private Integer currentEnrollments;
    private Double rating;
    private Integer reviewCount;
    private List<String> tags;
    private List<String> requirements;
    private List<String> outcomes;
    private String language;
    private List<String> subtitles;
    private String thumbnailUrl;
    private String previewVideoUrl;
    private String status;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private List<Course.CourseModule> modules;

    // Additional fields for UI
    private Boolean isEnrolled;
    private Double userProgress;
}