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
public class CourseCreateModuleRequest {

    @NotBlank(message = "Module title is required")
    @Size(min = 3, max = 100, message = "Module title must be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Module description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    private Boolean isPreview;
    private String moduleType;
    private String content;
    private String instructions;
    private Integer maxAttempts;
    private Boolean isRequired;
}