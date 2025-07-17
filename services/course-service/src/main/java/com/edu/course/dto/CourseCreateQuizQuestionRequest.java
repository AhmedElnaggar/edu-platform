package com.edu.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateQuizQuestionRequest {

    @NotBlank(message = "Question is required")
    @Size(min = 10, max = 1000, message = "Question must be between 10 and 1000 characters")
    private String question;

    @NotBlank(message = "Question type is required")
    private String questionType;

    private List<QuizOptionCreateRequest> options;
    private String correctAnswer;
    private String explanation;

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;

    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;

    private String difficulty;
    private Integer timeLimit;
    private Boolean isRequired;
    private List<String> tags;
    private String imageUrl;
    private String videoUrl;
    private String audioUrl;
}

