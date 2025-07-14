package com.edu.course.controller;

import com.edu.course.dto.EnrollmentDto;
import com.edu.course.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<EnrollmentDto> enrollInCourse(@PathVariable String courseId) {
        String userId = getCurrentUserId();
        String authHeader = getAuthHeader();

        log.info("Enrolling user: {} in course: {}", userId, courseId);

        EnrollmentDto enrollment = enrollmentService.enrollUser(courseId, userId, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @DeleteMapping("/{courseId}/unenroll")
    public ResponseEntity<Map<String, String>> unenrollFromCourse(@PathVariable String courseId) {
        String userId = getCurrentUserId();

        log.info("Unenrolling user: {} from course: {}", userId, courseId);

        enrollmentService.unenrollUser(courseId, userId);
        return ResponseEntity.ok(Map.of("message", "Successfully unenrolled from course"));
    }

    @GetMapping("/{courseId}/enrollment-status")
    public ResponseEntity<Map<String, Boolean>> getEnrollmentStatus(@PathVariable String courseId) {
        String userId = getCurrentUserId();

        boolean isEnrolled = enrollmentService.isUserEnrolled(userId, courseId);
        return ResponseEntity.ok(Map.of("isEnrolled", isEnrolled));
    }

    @GetMapping("/my-enrollments")
    public ResponseEntity<Page<EnrollmentDto>> getMyEnrollments(@PageableDefault(size = 20) Pageable pageable) {
        String userId = getCurrentUserId();

        log.info("Fetching enrollments for user: {}", userId);

        Page<EnrollmentDto> enrollments = enrollmentService.getUserEnrollments(userId, pageable);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/{courseId}/enrollments")
    public ResponseEntity<Page<EnrollmentDto>> getCourseEnrollments(
            @PathVariable String courseId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Fetching enrollments for course: {}", courseId);

        Page<EnrollmentDto> enrollments = enrollmentService.getCourseEnrollments(courseId, pageable);
        return ResponseEntity.ok(enrollments);
    }

    @PutMapping("/enrollments/{enrollmentId}/progress")
    public ResponseEntity<EnrollmentDto> updateProgress(
            @PathVariable String enrollmentId,
            @RequestBody Map<String, Double> progressRequest) {

        String userId = getCurrentUserId();
        Double progress = progressRequest.get("progress");

        log.info("Updating progress for enrollment: {} to {}%", enrollmentId, progress);

        EnrollmentDto enrollment = enrollmentService.updateProgress(enrollmentId, userId, progress);
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping("/stats/enrollments")
    public ResponseEntity<Map<String, Long>> getEnrollmentStats() {
        String userId = getCurrentUserId();

        long enrollmentCount = enrollmentService.getUserEnrollmentCount(userId);
        return ResponseEntity.ok(Map.of("totalEnrollments", enrollmentCount));
    }

    private String getCurrentUserId() {
        // Mock implementation - extract from JWT in real app
        return "00000000-0000-0000-0000-000000000001";
    }

    private String getAuthHeader() {
        return "Bearer mock-token";
    }
}