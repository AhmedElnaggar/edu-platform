package com.edu.course.service;

import com.edu.course.client.UserServiceClient;
import com.edu.course.document.Course;
import com.edu.course.dto.*;
import com.edu.course.events.CourseEventPublisher;
import com.edu.course.exception.CourseNotFoundException;
import com.edu.course.exception.UnauthorizedAccessException;
import com.edu.course.repository.CourseRepository;
import com.edu.course.repository.EnrollmentRepository;
import com.edu.course.utils.CourseValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserServiceClient userServiceClient;
    private final CourseEventPublisher eventPublisher;
    private final CourseValidator courseValidator;

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

        // Check if user is enrolled (if user is provided)
        if (userId != null) {
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
            dto.setIsEnrolled(isEnrolled);

            if (isEnrolled) {
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
        log.info("Creating course '{}' for instructor: {}", request.getTitle(), instructorId);

        // Validate the request
        courseValidator.validateCreateRequest(request);

        // Verify instructor exists
        try {
            userServiceClient.checkUserExists(instructorId, authHeader);
        } catch (Exception e) {
            log.error("Failed to verify instructor: {}", instructorId, e);
            throw new UnauthorizedAccessException("Invalid instructor");
        }

        // Create course
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        course = courseRepository.save(course);

        // Publish course created event
        eventPublisher.publishCourseCreated(course);

        log.info("Course created successfully with id: {}", course.getId());
        return convertToDto(course);
    }

    @Transactional
    public CourseDto updateCourse(String courseId, CreateCourseRequest request, String instructorId) {
        log.info("Updating course: {} by instructor: {}", courseId, instructorId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        // Check if instructor owns the course
        if (!course.getInstructorId().equals(instructorId)) {
            throw new UnauthorizedAccessException("You can only update your own courses");
        }

        // Validate the update request
        courseValidator.validateUpdateRequest(request, course);

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
        course.setUpdatedAt(LocalDateTime.now());

        course = courseRepository.save(course);

        // Publish course updated event
        eventPublisher.publishCourseUpdated(course);

        log.info("Course updated successfully: {}", courseId);
        return convertToDto(course);
    }

    @Transactional
    public void publishCourse(String courseId, String instructorId) {
        log.info("Publishing course: {} by instructor: {}", courseId, instructorId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        // Check if instructor owns the course
        if (!course.getInstructorId().equals(instructorId)) {
            throw new UnauthorizedAccessException("You can only publish your own courses");
        }

        // Validate course can be published
        courseValidator.validateCourseForPublishing(course);

        course.setStatus("PUBLISHED");
        course.setPublishedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        // Publish course published event
        eventPublisher.publishCoursePublished(course);

        log.info("Course published successfully: {}", courseId);
    }

    @Transactional
    public void deleteCourse(String courseId, String instructorId) {
        log.info("Deleting course: {} by instructor: {}", courseId, instructorId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        // Check if instructor owns the course
        if (!course.getInstructorId().equals(instructorId)) {
            throw new UnauthorizedAccessException("You can only delete your own courses");
        }

        // Check if course has enrollments
        long enrollmentCount = enrollmentRepository.countByCourseId(courseId);
        if (enrollmentCount > 0) {
            // Soft delete - mark as inactive
            course.setActive(false);
            course.setUpdatedAt(LocalDateTime.now());
            courseRepository.save(course);
            log.info("Course soft-deleted due to enrollments: {}", courseId);
        } else {
            // Hard delete
            courseRepository.delete(course);
            log.info("Course hard-deleted: {}", courseId);
        }

        // Publish course deleted event
        eventPublisher.publishCourseDeleted(courseId, instructorId);
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
                .modules(convertModulesToDto(course.getModules()))
                // Additional metadata
                .level(course.getLevel())
                .certificateEnabled(course.getCertificateEnabled())
                .prerequisites(course.getPrerequisites())
                .totalLessons(course.getTotalLessons())
                .totalVideos(course.getTotalVideos())
                .lastUpdatedBy(course.getLastUpdatedBy())
                // SEO fields
                .metaTitle(course.getMetaTitle())
                .metaDescription(course.getMetaDescription())
                .keywords(course.getKeywords())
                // Statistics
                .totalViews(course.getTotalViews())
                .totalWishlists(course.getTotalWishlists())
                .lastViewedAt(course.getLastViewedAt())
                .build();
    }

    private List<CourseModuleDto> convertModulesToDto(List<Course.CourseModule> modules) {
        if (modules == null) {
            return null;
        }
        return modules.stream()
                .map(this::convertModuleToDto)
                .collect(Collectors.toList());
    }

    private CourseModuleDto convertModuleToDto(Course.CourseModule module) {
        return CourseModuleDto.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .orderIndex(module.getOrderIndex())
                .duration(module.getDuration())
                .isPreview(module.getIsPreview())
                .lessons(convertLessonsToDto(module.getLessons()))
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .createdBy(module.getCreatedBy())
                .lastUpdatedBy(module.getLastUpdatedBy())
                .isActive(module.getIsActive())
                .moduleType(module.getModuleType())
                .totalLessons(module.getTotalLessons())
                .completedLessons(module.getCompletedLessons())
                .completionPercentage(module.getCompletionPercentage())
                .content(module.getContent())
                .instructions(module.getInstructions())
                .maxAttempts(module.getMaxAttempts())
                .isRequired(module.getIsRequired())
                .build();
    }

    private List<CourseLessonDto> convertLessonsToDto(List<Course.CourseLesson> lessons) {
        if (lessons == null) {
            return null;
        }
        return lessons.stream()
                .map(this::convertLessonToDto)
                .collect(Collectors.toList());
    }

    private CourseLessonDto convertLessonToDto(Course.CourseLesson lesson) {
        return CourseLessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .videoUrl(lesson.getVideoUrl())
                .duration(lesson.getDuration())
                .orderIndex(lesson.getOrderIndex())
                .isPreview(lesson.getIsPreview())
                .contentType(lesson.getContentType())
                .resources(convertResourcesToDto(lesson.getResources()))
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .createdBy(lesson.getCreatedBy())
                .lastUpdatedBy(lesson.getLastUpdatedBy())
                .isActive(lesson.getIsActive())
                .videoQuality(lesson.getVideoQuality())
                .transcriptUrl(lesson.getTranscriptUrl())
                .captionsUrl(lesson.getCaptionsUrl())
                .viewCount(lesson.getViewCount())
                .isCompleted(lesson.getIsCompleted())
                .averageWatchTime(lesson.getAverageWatchTime())
                .maxScore(lesson.getMaxScore())
                .passingScore(lesson.getPassingScore())
                .isGraded(lesson.getIsGraded())
                .assignmentInstructions(lesson.getAssignmentInstructions())
                .quizQuestions(convertQuizQuestionsToDto(lesson.getQuizQuestions()))
                .videoProvider(lesson.getVideoProvider())
                .videoId(lesson.getVideoId())
                .allowDownload(lesson.getAllowDownload())
                .videoThumbnail(lesson.getVideoThumbnail())
                .textContent(lesson.getTextContent())
                .htmlContent(lesson.getHtmlContent())
                .isRequired(lesson.getIsRequired())
                .minWatchTime(lesson.getMinWatchTime())
                .allowSkip(lesson.getAllowSkip())
                .build();
    }

    private List<CourseResourceDto> convertResourcesToDto(List<Course.CourseResource> resources) {
        if (resources == null) {
            return null;
        }
        return resources.stream()
                .map(this::convertResourceToDto)
                .collect(Collectors.toList());
    }

    private CourseResourceDto convertResourceToDto(Course.CourseResource resource) {
        return CourseResourceDto.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .url(resource.getUrl())
                .type(resource.getType())
                .size(resource.getSize())
                .mimeType(resource.getMimeType())
                .createdAt(resource.getCreatedAt())
                .createdBy(resource.getCreatedBy())
                .isDownloadable(resource.getIsDownloadable())
                .isExternal(resource.getIsExternal())
                .fileExtension(resource.getFileExtension())
                .downloadCount(resource.getDownloadCount())
                .isActive(resource.getIsActive())
                .originalFileName(resource.getOriginalFileName())
                .storagePath(resource.getStoragePath())
                .checksum(resource.getChecksum())
                .isProcessed(resource.getIsProcessed())
                .thumbnailUrl(resource.getThumbnailUrl())
                .provider(resource.getProvider())
                .isRequired(resource.getIsRequired())
                .accessLevel(resource.getAccessLevel())
                .expiresAt(resource.getExpiresAt())
                .build();
    }

    private List<QuizQuestionDto> convertQuizQuestionsToDto(List<Course.QuizQuestion> questions) {
        if (questions == null) {
            return null;
        }
        return questions.stream()
                .map(this::convertQuizQuestionToDto)
                .collect(Collectors.toList());
    }

    private QuizQuestionDto convertQuizQuestionToDto(Course.QuizQuestion question) {
        return QuizQuestionDto.builder()
                .id(question.getId())
                .question(question.getQuestion())
                .questionType(question.getQuestionType())
                .options(convertQuizOptionsToDto(question.getOptions()))
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .points(question.getPoints())
                .orderIndex(question.getOrderIndex())
                .createdAt(question.getCreatedAt())
                .createdBy(question.getCreatedBy())
                .isActive(question.getIsActive())
                .difficulty(question.getDifficulty())
                .timeLimit(question.getTimeLimit())
                .isRequired(question.getIsRequired())
                .tags(question.getTags())
                .imageUrl(question.getImageUrl())
                .videoUrl(question.getVideoUrl())
                .audioUrl(question.getAudioUrl())
                .build();
    }

    private List<QuizOptionDto> convertQuizOptionsToDto(List<Course.QuizOption> options) {
        if (options == null) {
            return null;
        }
        return options.stream()
                .map(this::convertQuizOptionToDto)
                .collect(Collectors.toList());
    }

    private QuizOptionDto convertQuizOptionToDto(Course.QuizOption option) {
        return QuizOptionDto.builder()
                .id(option.getId())
                .text(option.getText())
                .isCorrect(option.getIsCorrect())
                .orderIndex(option.getOrderIndex())
                .explanation(option.getExplanation())
                .imageUrl(option.getImageUrl())
                .isActive(option.getIsActive())
                .build();
    }
}