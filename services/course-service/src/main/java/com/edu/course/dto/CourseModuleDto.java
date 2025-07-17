package com.edu.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseModuleDto {

    private String id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Integer duration;
    private Boolean isPreview;
    private List<CourseLessonDto> lessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String lastUpdatedBy;

    // Module metadata
    private Boolean isActive;
    private String moduleType;
    private Integer totalLessons;
    private Integer completedLessons;
    private Double completionPercentage;

    // Module content
    private String content;
    private String instructions;
    private Integer maxAttempts;
    private Boolean isRequired;
}
