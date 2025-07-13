package com.edu.course.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    private String id;

    private String courseId;
    private String userId;
    private LocalDateTime enrolledAt;
    private String status; // ENROLLED, COMPLETED, CANCELLED
    private Double progress = 0.0;
}