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
public class QuizQuestionDto {

    private String id;
    private String question;
    private String questionType;
    private List<QuizOptionDto> options;
    private String correctAnswer;
    private String explanation;
    private Integer points;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private String createdBy;

    // Question metadata
    private Boolean isActive;
    private String difficulty;
    private Integer timeLimit;
    private Boolean isRequired;
    private List<String> tags;

    // Media support
    private String imageUrl;
    private String videoUrl;
    private String audioUrl;
}