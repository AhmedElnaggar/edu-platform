package com.edu.course.service;

import com.edu.course.client.UserServiceClient;
import com.edu.course.document.Course;
import com.edu.course.dto.CourseDto;
import com.edu.course.dto.CreateCourseRequest;
import com.edu.course.events.CourseEventPublisher;
import com.edu.course.exception.CourseNotFoundException;
import com.edu.course.exception.UnauthorizedAccessException;
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
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserServiceClient userServiceClient;
    private final CourseEventPublisher eventPublisher;

    public Page<CourseDto> getAllPublishedCourses(Pageable pageable) {
        log.info("Fetching all published courses");

        Page<Course> courses = courseRepository.findByActiveTrueAndStatus("PUBLISHED", pageable);
        return courses.map(this::convertToDto);
    }

    public CourseDto getCourseById(String courseId, String userId) {
        log.info("Fetching course by id: {} for user: {}", courseId, userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        CourseDto dto = convertToDto(course);

        // Check if user is enrolled
        if (userId != null) {
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
            dto.setIsEnrolled(isEnrolled);

            if (isEnrolled) {
                // Get user progress
                enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                        .ifPresent(enrollment -> dto.setUserProgress(enrollment.getProgress()));
            }
        }

        return dto;
    }

    public Page<CourseDto> searchCourses(String searchTerm, Pageable pageable) {
        log.info("Searching courses with term: {}", searchTerm);

        Page<Course> courses = courseRepository.searchCourses(searchTerm, pageable);
        return courses.map(this::convertToDto);
    }

    public Page<CourseDto> getCoursesByCategory(String category, Pageable pageable) {
        log.info("Fetching courses by category: {}", category);

        Page<Course> courses = courseRepository.findByTagsIn(List.of(category), pageable);
        return courses.map(this::convertToDto);
    }

    public List<CourseDto> getCoursesByInstructor(String instructorId) {
        log.info("Fetching courses by instructor: {}", instructorId);

        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        return courses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseDto createCourse(CreateCourseRequest request, String instructorId, String authHeader) {
        log.info("Creating course for instructor: {}", instructorId);

        // Verify instructor exists
        try {
            userServiceClient.checkUserExists(UUID.fromString(instructorId), authHeader);
        } catch (Exception e) {
            throw new UnauthorizedAccessException("Invalid instructor");
        }

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .instructorId(instructorId)
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .duration(request.getDuration())
                .maxStudents(request.getMaxStudents())
                .tags(request.getTags())
                .requirements(request.getRequirements())
                .outcomes(request.getOutcomes())
                .language(request.getLanguage())
                .subtitles(request.getSubtitles())
                .thumbnailUrl(request.getThumbnailUrl())
                .previewVideoUrl(request.getPreviewVideoUrl())
                .status("DRAFT")
                .active(true)
                .currentEnrollments(0)
                .rating(0.0)
                .reviewCount(0)
                .build();

        course = courseRepository.save(course);

        // Publish event
        eventPublisher.publishCourseCreated(course);

        log.info("Course created successfully with id: {}", course.getId());
        return convertToDto(course);
    }

    @Transactional
    public CourseDto updateCourse(String courseId, CreateCourseRequest request, String instructorId) {
        log.info("Updating course: {} by instructor: {}", courseId, instructorId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        // Verify instructor owns the course
        if (!course.getInstructorId().equals(instructorId)) {
            throw new UnauthorizedAccessException("You can only update your own courses");
        }

        // Update course fields
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setShortDescription(request.getShortDescription());
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty());
        course.setPrice(request.getPrice());
        course.setDuration(request.getDuration());
        course.setMaxStudents(request.getMaxStudents());
        course.setTags(request.getTags());
        course.setRequirements(request.getRequirements());
        course.setOutcomes(request.getOutcomes());
        course.setLanguage(request.getLanguage());
        course.setSubtitles(request.getSubtitles());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setPreviewVideoUrl(request.getPreviewVideoUrl());

        course = courseRepository.save(course);

        // Publish event
        eventPublisher.publishCourseUpdated(course);

        log.info("Course updated successfully: {}", courseId);
        return convertToDto(course);
    }

    @Transactional
    public void publishCourse(String courseId, String instructorId) {
        log.info("Publishing course: {} by instructor: {}", courseId, instructorId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        // Verify instructor owns the course
        if (!course.getInstructorId().equals(instructorId)) {
            throw new UnauthorizedAccessException("You can only publish your own courses");
        }

        course.setStatus("PUBLISHED");
        course.setPublishedAt(LocalDateTime.now());
        courseRepository.save(course);

        // Publish event
        eventPublisher.publishCoursePublished(course);

        log.info("Course published successfully: {}", courseId);
    }

    @Transactional
    public void deleteCourse(String courseId, String instructorId) {
        log.info("Deleting course: {} by instructor: {}", courseId, instructorId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        // Verify instructor owns the course
        if (!course.getInstructorId().equals(instructorId)) {
            throw new UnauthorizedAccessException("You can only delete your own courses");
        }

        // Check if course has enrollments
        long enrollmentCount = enrollmentRepository.countByCourseId(courseId);
        if (enrollmentCount > 0) {
            // Soft delete - mark as inactive
            course.setActive(false);
            courseRepository.save(course);
        } else {
            // Hard delete
            courseRepository.delete(course);
        }

        // Publish event
        // Publish event
        eventPublisher.publishCourseDeleted(courseId, instructorId);

        log.info("Course deleted successfully: {}", courseId);
    }

    public long getCourseCountByInstructor(String instructorId) {
        return courseRepository.countByInstructorId(instructorId);
    }

    public long getTotalCourseCount() {
        return courseRepository.count();
    }

    private CourseDto convertToDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .shortDescription(course.getShortDescription())
                .instructorId(course.getInstructorId())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .price(course.getPrice())
                .currency(course.getCurrency())
                .discountPrice(course.getDiscountPrice())
                .discountExpiry(course.getDiscountExpiry())
                .duration(course.getDuration())
                .maxStudents(course.getMaxStudents())
                .currentEnrollments(course.getCurrentEnrollments())
                .rating(course.getRating())
                .reviewCount(course.getReviewCount())
                .tags(course.getTags())
                .requirements(course.getRequirements())
                .outcomes(course.getOutcomes())
                .language(course.getLanguage())
                .subtitles(course.getSubtitles())
                .thumbnailUrl(course.getThumbnailUrl())
                .previewVideoUrl(course.getPreviewVideoUrl())
                .status(course.getStatus())
                .active(course.getActive())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .publishedAt(course.getPublishedAt())
                .modules(course.getModules())
                .build();
    }
}