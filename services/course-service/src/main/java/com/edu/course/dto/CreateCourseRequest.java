package com.edu.course.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateCourseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @NotBlank(message = "Category is required")
    private String category;

    private String difficulty = "BEGINNER";

    @DecimalMin(value = "0.0", message = "Price must be positive")
    private Double price;

    private String currency = "USD";

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @Min(value = 1, message = "Maximum students must be at least 1")
    private Integer maxStudents;

    private List<String> tags;
    private List<String> requirements;
    private List<String> outcomes;

    private String language = "English";
    private List<String> subtitles;

    private String thumbnailUrl;
    private String previewVideoUrl;
}