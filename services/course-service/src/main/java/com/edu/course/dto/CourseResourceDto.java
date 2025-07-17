package com.edu.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResourceDto {

    private String id;
    private String title;
    private String description;
    private String url;
    private String type;
    private Long size;
    private String mimeType;
    private LocalDateTime createdAt;
    private String createdBy;

    // Resource metadata
    private Boolean isDownloadable;
    private Boolean isExternal;
    private String fileExtension;
    private Integer downloadCount;
    private Boolean isActive;

    // File specific fields
    private String originalFileName;
    private String storagePath;
    private String checksum;
    private Boolean isProcessed;
    private String thumbnailUrl;

    // Additional fields
    private String provider;
    private Boolean isRequired;
    private String accessLevel;
    private LocalDateTime expiresAt;
}