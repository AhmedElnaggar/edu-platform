package com.edu.course.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;

    @NotBlank(message = "Short description is required")
    @Size(min = 10, max = 200, message = "Short description must be between 10 and 200 characters")
    private String shortDescription;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer duration;

    @NotNull(message = "Max students is required")
    @Min(value = 1, message = "Max students must be at least 1")
    private Integer maxStudents;

    private List<String> tags;
    private List<String> requirements;
    private List<String> outcomes;

    @NotBlank(message = "Language is required")
    private String language;

    private List<String> subtitles;
    private String thumbnailUrl;
    private String previewVideoUrl;
}