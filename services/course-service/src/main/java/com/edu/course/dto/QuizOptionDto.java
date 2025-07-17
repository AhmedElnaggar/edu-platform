package com.edu.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionDto {

    private String id;
    private String text;
    private Boolean isCorrect;
    private Integer orderIndex;

    // Option metadata
    private String explanation;
    private String imageUrl;
    private Boolean isActive;
}