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
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "enrollments")
@CompoundIndex(def = "{'userId': 1, 'courseId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    private String id;

    @Indexed
    private String courseId;

    @Indexed
    private String userId;

    @Builder.Default
    @Indexed
    private String status = "ENROLLED"; // ENROLLED, IN_PROGRESS, COMPLETED, CANCELLED, REFUNDED

    @Builder.Default
    private Double progress = 0.0; // 0.0 to 100.0

    private BigDecimal amountPaid;
    private String paymentId;
    private String paymentMethod;

    @CreatedDate
    private LocalDateTime enrolledAt;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Progress tracking
    private Map<String, LessonProgress> lessonProgress;

    // Certificate info
    private String certificateId;
    private LocalDateTime certificateIssuedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonProgress {
        private String lessonId;
        private Boolean completed;
        private Double progress; // 0.0 to 100.0
        private Integer timeSpent; // in seconds
        private LocalDateTime lastAccessedAt;
        private LocalDateTime completedAt;
    }
}