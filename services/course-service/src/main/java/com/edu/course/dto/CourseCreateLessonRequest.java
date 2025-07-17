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
public class CourseCreateLessonRequest {

    @NotBlank(message = "Lesson title is required")
    @Size(min = 3, max = 100, message = "Lesson title must be between 3 and 100 characters")
    private String title;

    @Size(max = 1000, message = "Lesson description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;

    @NotBlank(message = "Content type is required")
    private String contentType;

    private String videoUrl;
    private Integer duration;
    private Boolean isPreview;
    private String videoQuality;
    private String transcriptUrl;
    private String captionsUrl;
    private String textContent;
    private String htmlContent;
    private Boolean isRequired;
    private Integer minWatchTime;
    private Boolean allowSkip;
    private Boolean allowDownload;
}
