package com.edu.course.service;

import com.edu.course.client.UserServiceClient;
import com.edu.course.document.Course;
import com.edu.course.document.Enrollment;
import com.edu.course.dto.EnrollmentDto;
import com.edu.course.events.CourseEventPublisher;
import com.edu.course.exception.CourseNotFoundException;
import com.edu.course.exception.EnrollmentException;
import com.edu.course.repository.CourseRepository;
import com.edu.course.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserServiceClient userServiceClient;
    private final CourseEventPublisher eventPublisher;

    @Transactional
    public EnrollmentDto enrollUser(String courseId, String userId, String authHeader) {
        log.info("Enrolling user: {} in course: {}", userId, courseId);

        // Verify course exists and is published
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        if (!"PUBLISHED".equals(course.getStatus()) || !course.getActive()) {
            throw new EnrollmentException("Course is not available for enrollment");
        }

        // Check if user already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new EnrollmentException("User is already enrolled in this course");
        }

        // Check course capacity
        if (course.getCurrentEnrollments() >= course.getMaxStudents()) {
            throw new EnrollmentException("Course is full");
        }

        // Verify user exists
        try {
            userServiceClient.checkUserExists(userId, authHeader);
        } catch (Exception e) {
            throw new EnrollmentException("Invalid user");
        }

        // Create enrollment
        Enrollment enrollment = Enrollment.builder()
                .courseId(courseId)
                .userId(userId)
                .status("ENROLLED")
                .progress(0.0)
                .amountPaid(course.getPrice())
                .paymentMethod("FREE") // For now, assuming free enrollment
                .enrolledAt(LocalDateTime.now())
                .build();

        enrollment = enrollmentRepository.save(enrollment);

        // Update course enrollment count
        course.setCurrentEnrollments(course.getCurrentEnrollments() + 1);
        courseRepository.save(course);

        // Publish event
        eventPublisher.publishCourseEnrolled(enrollment, course);

        log.info("User enrolled successfully: {} in course: {}", userId, courseId);
        return convertToDto(enrollment, course);
    }

    public List<EnrollmentDto> getUserEnrollments(String userId) {
        log.info("Fetching enrollments for user: {}", userId);

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        return enrollments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<EnrollmentDto> getUserEnrollments(String userId, Pageable pageable) {
        log.info("Fetching enrollments for user: {} with pagination", userId);

        Page<Enrollment> enrollments = enrollmentRepository.findByUserId(userId, pageable);
        return enrollments.map(this::convertToDto);
    }

    public Page<EnrollmentDto> getCourseEnrollments(String courseId, Pageable pageable) {
        log.info("Fetching enrollments for course: {}", courseId);

        Page<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId,pageable);
        return enrollments.map(this::convertToDto);
    }

    @Transactional
    public EnrollmentDto updateProgress(String enrollmentId, String userId, Double progress) {
        log.info("Updating progress for enrollment: {} to {}%", enrollmentId, progress);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentException("Enrollment not found"));

        // Verify user owns the enrollment
        if (!enrollment.getUserId().equals(userId)) {
            throw new EnrollmentException("You can only update your own progress");
        }

        enrollment.setProgress(progress);
        enrollment.setLastAccessedAt(LocalDateTime.now());

        // Mark as completed if progress is 100%
        if (progress >= 100.0 && !"COMPLETED".equals(enrollment.getStatus())) {
            enrollment.setStatus("COMPLETED");
            enrollment.setCompletedAt(LocalDateTime.now());

            // Publish completion event
            Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);
            if (course != null) {
                eventPublisher.publishCourseCompleted(enrollment, course);
            }
        }

        enrollment = enrollmentRepository.save(enrollment);

        return convertToDto(enrollment);
    }

    @Transactional
    public void unenrollUser(String courseId, String userId) {
        log.info("Unenrolling user: {} from course: {}", userId, courseId);

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new EnrollmentException("Enrollment not found"));

        enrollment.setStatus("CANCELLED");
        enrollmentRepository.save(enrollment);

        // Update course enrollment count
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            course.setCurrentEnrollments(Math.max(0, course.getCurrentEnrollments() - 1));
            courseRepository.save(course);

            // Publish event
            eventPublisher.publishCourseUnenrolled(enrollment, course);
        }

        log.info("User unenrolled successfully: {} from course: {}", userId, courseId);
    }

    public boolean isUserEnrolled(String userId, String courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    public long getUserEnrollmentCount(String userId) {
        return enrollmentRepository.findByUserId(userId).size();
    }

    public long getCourseEnrollmentCount(String courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

    private EnrollmentDto convertToDto(Enrollment enrollment) {
        return convertToDto(enrollment, null);
    }

    private EnrollmentDto convertToDto(Enrollment enrollment, Course course) {
        EnrollmentDto dto = EnrollmentDto.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourseId())
                .userId(enrollment.getUserId())
                .status(enrollment.getStatus())
                .progress(enrollment.getProgress())
                .amountPaid(enrollment.getAmountPaid())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .lastAccessedAt(enrollment.getLastAccessedAt())
                .build();

        // Add course info if provided
        if (course != null) {
            dto.setCourseTitle(course.getTitle());
            dto.setCourseThumbnail(course.getThumbnailUrl());
        }

        return dto;
    }
}