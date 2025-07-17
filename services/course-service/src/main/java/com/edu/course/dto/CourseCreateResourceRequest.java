package com.edu.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateResourceRequest {

    @NotBlank(message = "Resource title is required")
    @Size(min = 3, max = 100, message = "Resource title must be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Resource description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Resource URL is required")
    private String url;

    @NotBlank(message = "Resource type is required")
    private String type;

    private Long size;
    private String mimeType;
    private Boolean isDownloadable;
    private Boolean isExternal;
    private String originalFileName;
    private String provider;
    private Boolean isRequired;
    private String accessLevel;
    private LocalDateTime expiresAt;
}