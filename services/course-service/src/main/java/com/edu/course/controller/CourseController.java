package com.edu.course.controller;

import com.edu.course.dto.CourseDto;
import com.edu.course.dto.CreateCourseRequest;
import com.edu.course.exception.CourseNotFoundException;
import com.edu.course.exception.CourseValidationException;
import com.edu.course.exception.UnauthorizedAccessException;
import com.edu.course.service.CourseService;
import jakarta.validation.Valid;
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
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<Page<CourseDto>> getAllCourses(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching all published courses");
        Page<CourseDto> courses = courseService.getAllPublishedCourses(pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable String courseId,
                                                   @RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("Fetching course by id: {}", courseId);
        CourseDto course = courseService.getCourseById(courseId, userId);
        return ResponseEntity.ok(course);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CourseDto>> searchCourses(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Searching courses with query: {}", q);
        Page<CourseDto> courses = courseService.searchCourses(q, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<CourseDto>> getCoursesByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching courses by category: {}", category);
        Page<CourseDto> courses = courseService.getCoursesByCategory(category, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseDto>> getCoursesByInstructor(@PathVariable String instructorId) {
        log.info("Fetching courses by instructor: {}", instructorId);
        List<CourseDto> courses = courseService.getCoursesByInstructor(instructorId);
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    public ResponseEntity<CourseDto> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @RequestHeader("X-User-Id") String instructorId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("Creating course '{}' for instructor: {}", request.getTitle(), instructorId);
        CourseDto course = courseService.createCourse(request, instructorId, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody CreateCourseRequest request,
            @RequestHeader("X-User-Id") String instructorId) {

        log.info("Updating course: {} by instructor: {}", courseId, instructorId);
        CourseDto course = courseService.updateCourse(courseId, request, instructorId);
        return ResponseEntity.ok(course);
    }

    @PostMapping("/{courseId}/publish")
    public ResponseEntity<Map<String, String>> publishCourse(
            @PathVariable String courseId,
            @RequestHeader("X-User-Id") String instructorId) {

        log.info("Publishing course: {} by instructor: {}", courseId, instructorId);
        courseService.publishCourse(courseId, instructorId);
        return ResponseEntity.ok(Map.of("message", "Course published successfully"));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Map<String, String>> deleteCourse(
            @PathVariable String courseId,
            @RequestHeader("X-User-Id") String instructorId) {

        log.info("Deleting course: {} by instructor: {}", courseId, instructorId);
        courseService.deleteCourse(courseId, instructorId);
        return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
    }

    @GetMapping("/stats/instructor")
    public ResponseEntity<Map<String, Long>> getInstructorStats(@RequestHeader("X-User-Id") String instructorId) {
        long courseCount = courseService.getCourseCountByInstructor(instructorId);
        return ResponseEntity.ok(Map.of("totalCourses", courseCount));
    }

    @GetMapping("/stats/total")
    public ResponseEntity<Map<String, Long>> getTotalStats() {
        long totalCourses = courseService.getTotalCourseCount();
        return ResponseEntity.ok(Map.of("totalCourses", totalCourses));
    }

    // Exception handlers
    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCourseNotFound(CourseNotFoundException ex) {
        log.error("Course not found: {}", ex.getMessage());

        Map<String, Object> error = Map.of(
                "error", "COURSE_NOT_FOUND",
                "message", ex.getMessage(),
                "timestamp", java.time.LocalDateTime.now(),
                "status", 404
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        log.error("Unauthorized access: {}", ex.getMessage());

        Map<String, Object> error = Map.of(
                "error", "UNAUTHORIZED_ACCESS",
                "message", ex.getMessage(),
                "timestamp", java.time.LocalDateTime.now(),
                "status", 403
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(CourseValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(CourseValidationException ex) {
        log.error("Course validation error: {}", ex.getMessage());

        Map<String, Object> error = Map.of(
                "error", "VALIDATION_ERROR",
                "message", ex.getMessage(),
                "timestamp", java.time.LocalDateTime.now(),
                "status", 400
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> error = Map.of(
                "error", "INTERNAL_SERVER_ERROR",
                "message", "An unexpected error occurred",
                "timestamp", java.time.LocalDateTime.now(),
                "status", 500
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}