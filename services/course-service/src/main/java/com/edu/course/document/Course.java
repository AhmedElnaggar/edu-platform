package com.edu.course.document;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Document(collection = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    private String id;

    @TextIndexed(weight = 2)
    private String title;

    @TextIndexed
    private String description;

    private String shortDescription;

    @Indexed
    private String instructorId;

    @Indexed
    private String category;

    @Builder.Default
    private String difficulty = "BEGINNER"; // BEGINNER, INTERMEDIATE, ADVANCED

    private Double price;
    private String currency = "USD";
    private Double discountPrice;
    private LocalDateTime discountExpiry;

    private Integer duration; // in minutes
    private Integer maxStudents;

    @Builder.Default
    private Integer currentEnrollments = 0;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    private List<String> tags;
    private List<String> requirements;
    private List<String> outcomes;

    private String language = "English";
    private List<String> subtitles;

    private String thumbnailUrl;
    private String previewVideoUrl;

    @Builder.Default
    @Indexed
    private String status = "DRAFT"; // DRAFT, PUBLISHED, ARCHIVED

    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    // Course structure
    private List<CourseModule> modules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseModule {
        private String id;
        private String title;
        private String description;
        private Integer orderIndex;
        private Integer duration; // in minutes
        private List<CourseLesson> lessons;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseLesson {
        private String id;
        private String title;
        private String description;
        private Integer orderIndex;
        private String type; // VIDEO, TEXT, QUIZ, ASSIGNMENT
        private String contentUrl;
        private Integer duration; // in minutes
        private Boolean isFree;
    }
}