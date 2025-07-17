package com.edu.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    private String id;
    private String title;
    private String description;
    private String shortDescription;
    private String instructorId;
    private String category;
    private String difficulty;
    private BigDecimal price;
    private String currency;
    private BigDecimal discountPrice;
    private LocalDateTime discountExpiry;
    private Integer duration;
    private Integer maxStudents;
    private Integer currentEnrollments;
    private Double rating;
    private Integer reviewCount;
    private List<String> tags;
    private List<String> requirements;
    private List<String> outcomes;
    private String language;
    private List<String> subtitles;
    private String thumbnailUrl;
    private String previewVideoUrl;
    private String status;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private List<CourseModuleDto> modules;

    // Additional metadata
    private String level;
    private Boolean certificateEnabled;
    private List<String> prerequisites;
    private Integer totalLessons;
    private Integer totalVideos;
    private String lastUpdatedBy;

    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private List<String> keywords;

    // Course statistics
    private Integer totalViews;
    private Integer totalWishlists;
    private LocalDateTime lastViewedAt;

    // User-specific fields (populated if user is authenticated)
    private Boolean isEnrolled;
    private Double userProgress;
    private Boolean isWishlisted;
    private Boolean canAccess;
    private Boolean isOwner;
}