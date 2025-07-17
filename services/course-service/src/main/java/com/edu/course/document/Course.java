package com.edu.course.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    private String id;

    @TextIndexed
    private String title;

    @TextIndexed
    private String description;

    private String shortDescription;

    @Indexed
    private String instructorId;

    @Indexed
    private String category;

    @Indexed
    private String difficulty;

    private BigDecimal price;

    private String currency;

    private BigDecimal discountPrice;

    private LocalDateTime discountExpiry;

    private Integer duration; // in hours

    private Integer maxStudents;

    @Builder.Default
    private Integer currentEnrollments = 0;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    @TextIndexed
    private List<String> tags;

    private List<String> requirements;

    private List<String> outcomes;

    @Indexed
    private String language;

    private List<String> subtitles;

    private String thumbnailUrl;

    private String previewVideoUrl;

    @Indexed
    private String status; // DRAFT, PUBLISHED, ARCHIVED

    @Indexed
    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime publishedAt;

    // Embedded documents
    private List<CourseModule> modules;

    // Additional metadata
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
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

    // Embedded Document Classes
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
        private Boolean isPreview;
        private List<CourseLesson> lessons;

        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

        @Builder.Default
        private LocalDateTime updatedAt = LocalDateTime.now();

        private String createdBy;
        private String lastUpdatedBy;

        // Module metadata
        private Boolean isActive;
        private String moduleType; // VIDEO, TEXT, QUIZ, ASSIGNMENT
        private Integer totalLessons;
        private Integer completedLessons;
        private Double completionPercentage;

        // Module content
        private String content; // For text-based modules
        private String instructions; // For assignments/quizzes
        private Integer maxAttempts; // For quizzes
        private Boolean isRequired;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseLesson {

        private String id;
        private String title;
        private String description;
        private String videoUrl;
        private Integer duration; // in minutes
        private Integer orderIndex;
        private Boolean isPreview;
        private String contentType; // VIDEO, TEXT, QUIZ, ASSIGNMENT
        private List<CourseResource> resources;

        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

        @Builder.Default
        private LocalDateTime updatedAt = LocalDateTime.now();

        private String createdBy;
        private String lastUpdatedBy;

        // Lesson metadata
        private Boolean isActive;
        private String videoQuality; // HD, SD, 4K
        private String transcriptUrl;
        private String captionsUrl;
        private Integer viewCount;
        private Boolean isCompleted;
        private Double averageWatchTime;

        // Quiz/Assignment specific fields
        private Integer maxScore;
        private Integer passingScore;
        private Boolean isGraded;
        private String assignmentInstructions;
        private List<QuizQuestion> quizQuestions;

        // Video specific fields
        private String videoProvider; // YOUTUBE, VIMEO, S3, etc.
        private String videoId;
        private Boolean allowDownload;
        private String videoThumbnail;

        // Text content
        private String textContent;
        private String htmlContent;

        // Lesson settings
        private Boolean isRequired;
        private Integer minWatchTime; // Minimum time to mark as completed
        private Boolean allowSkip;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseResource {

        private String id;
        private String title;
        private String description;
        private String url;
        private String type; // PDF, VIDEO, LINK, DOCUMENT, IMAGE, AUDIO
        private Long size; // in bytes
        private String mimeType;

        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

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
        private String provider; // AWS_S3, GOOGLE_DRIVE, DROPBOX, etc.
        private Boolean isRequired;
        private String accessLevel; // PUBLIC, ENROLLED, PREMIUM
        private LocalDateTime expiresAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizQuestion {

        private String id;
        private String question;
        private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, ESSAY
        private List<QuizOption> options;
        private String correctAnswer;
        private String explanation;
        private Integer points;
        private Integer orderIndex;

        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

        private String createdBy;

        // Question metadata
        private Boolean isActive;
        private String difficulty; // EASY, MEDIUM, HARD
        private Integer timeLimit; // in seconds
        private Boolean isRequired;
        private List<String> tags;

        // Media support
        private String imageUrl;
        private String videoUrl;
        private String audioUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizOption {

        private String id;
        private String text;
        private Boolean isCorrect;
        private Integer orderIndex;

        // Option metadata
        private String explanation;
        private String imageUrl;
        private Boolean isActive;
    }
}