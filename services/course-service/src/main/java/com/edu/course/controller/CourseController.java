package com.edu.course.controller;

import com.edu.course.dto.CourseDto;
import com.edu.course.dto.CreateCourseRequest;
import com.edu.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<CourseDto> getCourseById(@PathVariable String courseId) {
        log.info("Fetching course by id: {}", courseId);

        // Get current user (mock for now)
        String userId = getCurrentUserId();

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
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        String instructorId = getCurrentUserId();
        String authHeader = getAuthHeader();

        log.info("Creating course for instructor: {}", instructorId);

        CourseDto course = courseService.createCourse(request, instructorId, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody CreateCourseRequest request) {

        String instructorId = getCurrentUserId();

        log.info("Updating course: {} by instructor: {}", courseId, instructorId);

        CourseDto course = courseService.updateCourse(courseId, request, instructorId);
        return ResponseEntity.ok(course);
    }

    @PostMapping("/{courseId}/publish")
    public ResponseEntity<Map<String, String>> publishCourse(@PathVariable String courseId) {
        String instructorId = getCurrentUserId();

        log.info("Publishing course: {} by instructor: {}", courseId, instructorId);

        courseService.publishCourse(courseId, instructorId);
        return ResponseEntity.ok(Map.of("message", "Course published successfully"));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Map<String, String>> deleteCourse(@PathVariable String courseId) {
        String instructorId = getCurrentUserId();

        log.info("Deleting course: {} by instructor: {}", courseId, instructorId);

        courseService.deleteCourse(courseId, instructorId);
        return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
    }

    @GetMapping("/stats/instructor")
    public ResponseEntity<Map<String, Long>> getInstructorStats() {
        String instructorId = getCurrentUserId();

        long courseCount = courseService.getCourseCountByInstructor(instructorId);
        return ResponseEntity.ok(Map.of("totalCourses", courseCount));
    }

    @GetMapping("/stats/total")
    public ResponseEntity<Map<String, Long>> getTotalStats() {
        long totalCourses = courseService.getTotalCourseCount();
        return ResponseEntity.ok(Map.of("totalCourses", totalCourses));
    }

    private String getCurrentUserId() {
        // For now, return a mock user ID
        // In real implementation, extract from JWT token
        return "00000000-0000-0000-0000-000000000001";
    }

    private String getAuthHeader() {
        // Get from SecurityContext
        return "Bearer mock-token";
    }
}