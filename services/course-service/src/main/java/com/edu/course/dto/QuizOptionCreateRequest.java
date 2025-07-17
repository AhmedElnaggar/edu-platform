package com.edu.course.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionCreateRequest {

    @NotBlank(message = "Option text is required")
    @Size(min = 1, max = 500, message = "Option text must be between 1 and 500 characters")
    private String text;

    @NotNull(message = "Correct answer flag is required")
    private Boolean isCorrect;

    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;

    private String explanation;
    private String imageUrl;
}