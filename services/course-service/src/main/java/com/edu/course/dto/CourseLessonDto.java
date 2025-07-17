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
public class CourseLessonDto {

    private String id;
    private String title;
    private String description;
    private String videoUrl;
    private Integer duration;
    private Integer orderIndex;
    private Boolean isPreview;
    private String contentType;
    private List<CourseResourceDto> resources;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String lastUpdatedBy;

    // Lesson metadata
    private Boolean isActive;
    private String videoQuality;
    private String transcriptUrl;
    private String captionsUrl;
    private Integer viewCount;
    private Boolean isCompleted;
    private Double averageWatchTime;

    // Quiz/Assignment specific fields
    private Integer maxScore;
    private Integer passingScore;
    private Boolean isGraded;
    private String assignmentInstructions;
    private List<QuizQuestionDto> quizQuestions;

    // Video specific fields
    private String videoProvider;
    private String videoId;
    private Boolean allowDownload;
    private String videoThumbnail;

    // Text content
    private String textContent;
    private String htmlContent;

    // Lesson settings
    private Boolean isRequired;
    private Integer minWatchTime;
    private Boolean allowSkip;
}