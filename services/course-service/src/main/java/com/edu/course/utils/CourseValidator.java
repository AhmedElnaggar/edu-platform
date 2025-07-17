package com.edu.course.utils;

import com.edu.course.document.Course;
import com.edu.course.dto.CreateCourseRequest;
import com.edu.course.exception.CourseValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class CourseValidator {

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "PROGRAMMING", "DESIGN", "BUSINESS", "MARKETING", "MUSIC", "LIFESTYLE",
            "PHOTOGRAPHY", "HEALTH", "TEACHING", "DEVELOPMENT", "IT_SOFTWARE",
            "OFFICE_PRODUCTIVITY", "PERSONAL_DEVELOPMENT", "LANGUAGE", "TEST_PREP"
    );

    private static final List<String> VALID_DIFFICULTIES = Arrays.asList(
            "BEGINNER", "INTERMEDIATE", "ADVANCED", "ALL_LEVELS"
    );

    private static final List<String> VALID_LANGUAGES = Arrays.asList(
            "ENGLISH", "SPANISH", "FRENCH", "GERMAN", "ITALIAN", "PORTUGUESE",
            "RUSSIAN", "CHINESE", "JAPANESE", "KOREAN", "ARABIC", "HINDI"
    );

    private static final List<String> VALID_CURRENCIES = Arrays.asList(
            "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "BRL"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\s/$.?#].[^\s]*$", Pattern.CASE_INSENSITIVE
    );

    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MIN_DESCRIPTION_LENGTH = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MIN_SHORT_DESCRIPTION_LENGTH = 10;
    private static final int MAX_SHORT_DESCRIPTION_LENGTH = 200;
    private static final int MIN_DURATION_HOURS = 1;
    private static final int MAX_DURATION_HOURS = 1000;
    private static final int MIN_MAX_STUDENTS = 1;
    private static final int MAX_MAX_STUDENTS = 10000;
    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999.99");

    public void validateCreateRequest(CreateCourseRequest request) {
        log.debug("Validating create course request for: {}", request.getTitle());

        List<String> errors = new ArrayList<>();

        // Basic validations
        validateTitle(request.getTitle(), errors);
        validateDescription(request.getDescription(), errors);
        validateShortDescription(request.getShortDescription(), errors);
        validateCategory(request.getCategory(), errors);
        validateDifficulty(request.getDifficulty(), errors);
        validatePrice(request.getPrice(), errors);
        validateCurrency(request.getCurrency(), errors);
        validateDuration(request.getDuration(), errors);
        validateMaxStudents(request.getMaxStudents(), errors);
        validateLanguage(request.getLanguage(), errors);
        validateTags(request.getTags(), errors);
        validateRequirements(request.getRequirements(), errors);
        validateOutcomes(request.getOutcomes(), errors);
        validateSubtitles(request.getSubtitles(), errors);
        validateUrls(request.getThumbnailUrl(), request.getPreviewVideoUrl(), errors);

        if (!errors.isEmpty()) {
            String errorMessage = "Course validation failed: " + String.join(", ", errors);
            log.error("Course validation failed: {}", errorMessage);
            throw new CourseValidationException(errorMessage);
        }

        log.debug("Course validation passed for: {}", request.getTitle());
    }

    public void validateUpdateRequest(CreateCourseRequest request, Course existingCourse) {
        log.debug("Validating update course request for: {}", request.getTitle());

        // First validate the basic request
        validateCreateRequest(request);

        List<String> errors = new ArrayList<>();

        // Additional validation for updates
        if ("PUBLISHED".equals(existingCourse.getStatus()) &&
                existingCourse.getCurrentEnrollments() > 0) {

            // Restrict certain fields for published courses with enrollments
            if (!existingCourse.getPrice().equals(request.getPrice())) {
                errors.add("Cannot change price for published course with enrollments");
            }

            if (!existingCourse.getMaxStudents().equals(request.getMaxStudents()) &&
                    request.getMaxStudents() < existingCourse.getCurrentEnrollments()) {
                errors.add("Cannot reduce max students below current enrollments");
            }
        }

        if (!errors.isEmpty()) {
            String errorMessage = "Course update validation failed: " + String.join(", ", errors);
            log.error("Course update validation failed: {}", errorMessage);
            throw new CourseValidationException(errorMessage);
        }

        log.debug("Course update validation passed for: {}", request.getTitle());
    }

    public void validateCourseForPublishing(Course course) {
        log.debug("Validating course for publishing: {}", course.getId());

        List<String> errors = new ArrayList<>();

        // Check if course is already published
        if ("PUBLISHED".equals(course.getStatus())) {
            errors.add("Course is already published");
        }

        // Check if course is active
        if (!course.getActive()) {
            errors.add("Cannot publish inactive course");
        }

        // Validate required fields for publishing
        if (!StringUtils.hasText(course.getTitle())) {
            errors.add("Title is required for publishing");
        }

        if (!StringUtils.hasText(course.getDescription())) {
            errors.add("Description is required for publishing");
        }

        if (!StringUtils.hasText(course.getShortDescription())) {
            errors.add("Short description is required for publishing");
        }

        if (!StringUtils.hasText(course.getThumbnailUrl())) {
            errors.add("Thumbnail image is required for publishing");
        }

        if (course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Valid price is required for publishing");
        }

        if (course.getDuration() == null || course.getDuration() < 1) {
            errors.add("Valid duration is required for publishing");
        }

        // Check if course has content (modules)
        if (course.getModules() == null || course.getModules().isEmpty()) {
            errors.add("Course must have at least one module to be published");
        }

        // Validate learning outcomes
        if (course.getOutcomes() == null || course.getOutcomes().isEmpty()) {
            errors.add("Course must have learning outcomes to be published");
        }

        if (!errors.isEmpty()) {
            String errorMessage = "Course publishing validation failed: " + String.join(", ", errors);
            log.error("Course publishing validation failed: {}", errorMessage);
            throw new CourseValidationException(errorMessage);
        }

        log.debug("Course publishing validation passed for: {}", course.getId());
    }

    // Private validation methods
    private void validateTitle(String title, List<String> errors) {
        if (!StringUtils.hasText(title)) {
            errors.add("Title is required");
        } else if (title.length() < MIN_TITLE_LENGTH || title.length() > MAX_TITLE_LENGTH) {
            errors.add(String.format("Title must be between %d and %d characters",
                    MIN_TITLE_LENGTH, MAX_TITLE_LENGTH));
        }
    }

    private void validateDescription(String description, List<String> errors) {
        if (!StringUtils.hasText(description)) {
            errors.add("Description is required");
        } else if (description.length() < MIN_DESCRIPTION_LENGTH ||
                description.length() > MAX_DESCRIPTION_LENGTH) {
            errors.add(String.format("Description must be between %d and %d characters",
                    MIN_DESCRIPTION_LENGTH, MAX_DESCRIPTION_LENGTH));
        }
    }

    private void validateShortDescription(String shortDescription, List<String> errors) {
        if (!StringUtils.hasText(shortDescription)) {
            errors.add("Short description is required");
        } else if (shortDescription.length() < MIN_SHORT_DESCRIPTION_LENGTH ||
                shortDescription.length() > MAX_SHORT_DESCRIPTION_LENGTH) {
            errors.add(String.format("Short description must be between %d and %d characters",
                    MIN_SHORT_DESCRIPTION_LENGTH, MAX_SHORT_DESCRIPTION_LENGTH));
        }
    }

    private void validateCategory(String category, List<String> errors) {
        if (!StringUtils.hasText(category)) {
            errors.add("Category is required");
        } else if (!VALID_CATEGORIES.contains(category.toUpperCase())) {
            errors.add("Invalid category. Valid categories: " + VALID_CATEGORIES);
        }
    }

    private void validateDifficulty(String difficulty, List<String> errors) {
        if (!StringUtils.hasText(difficulty)) {
            errors.add("Difficulty is required");
        } else if (!VALID_DIFFICULTIES.contains(difficulty.toUpperCase())) {
            errors.add("Invalid difficulty. Valid difficulties: " + VALID_DIFFICULTIES);
        }
    }

    private void validatePrice(BigDecimal price, List<String> errors) {
        if (price == null) {
            errors.add("Price is required");
        } else if (price.compareTo(MIN_PRICE) < 0 || price.compareTo(MAX_PRICE) > 0) {
            errors.add(String.format("Price must be between %s and %s", MIN_PRICE, MAX_PRICE));
        }
    }

    private void validateCurrency(String currency, List<String> errors) {
        if (!StringUtils.hasText(currency)) {
            errors.add("Currency is required");
        } else if (!VALID_CURRENCIES.contains(currency.toUpperCase())) {
            errors.add("Invalid currency. Valid currencies: " + VALID_CURRENCIES);
        }
    }

    private void validateDuration(Integer duration, List<String> errors) {
        if (duration == null) {
            errors.add("Duration is required");
        } else if (duration < MIN_DURATION_HOURS || duration > MAX_DURATION_HOURS) {
            errors.add(String.format("Duration must be between %d and %d hours",
                    MIN_DURATION_HOURS, MAX_DURATION_HOURS));
        }
    }

    private void validateMaxStudents(Integer maxStudents, List<String> errors) {
        if (maxStudents == null) {
            errors.add("Max students is required");
        } else if (maxStudents < MIN_MAX_STUDENTS || maxStudents > MAX_MAX_STUDENTS) {
            errors.add(String.format("Max students must be between %d and %d",
                    MIN_MAX_STUDENTS, MAX_MAX_STUDENTS));
        }
    }

    private void validateLanguage(String language, List<String> errors) {
        if (!StringUtils.hasText(language)) {
            errors.add("Language is required");
        } else if (!VALID_LANGUAGES.contains(language.toUpperCase())) {
            errors.add("Invalid language. Valid languages: " + VALID_LANGUAGES);
        }
    }

    private void validateTags(List<String> tags, List<String> errors) {
        if (tags != null) {
            if (tags.size() > 10) {
                errors.add("Maximum 10 tags allowed");
            }

            for (String tag : tags) {
                if (!StringUtils.hasText(tag) || tag.length() > 30) {
                    errors.add("Each tag must be non-empty and max 30 characters");
                    break;
                }
            }
        }
    }

    private void validateRequirements(List<String> requirements, List<String> errors) {
        if (requirements != null && requirements.size() > 20) {
            errors.add("Maximum 20 requirements allowed");
        }
    }

    private void validateOutcomes(List<String> outcomes, List<String> errors) {
        if (outcomes != null && outcomes.size() > 30) {
            errors.add("Maximum 30 learning outcomes allowed");
        }
    }

    private void validateSubtitles(List<String> subtitles, List<String> errors) {
        if (subtitles != null) {
            for (String subtitle : subtitles) {
                if (!VALID_LANGUAGES.contains(subtitle.toUpperCase())) {
                    errors.add("Invalid subtitle language: " + subtitle);
                    break;
                }
            }
        }
    }

    private void validateUrls(String thumbnailUrl, String previewVideoUrl, List<String> errors) {
        if (StringUtils.hasText(thumbnailUrl) && !isValidUrl(thumbnailUrl)) {
            errors.add("Invalid thumbnail URL format");
        }

        if (StringUtils.hasText(previewVideoUrl) && !isValidUrl(previewVideoUrl)) {
            errors.add("Invalid preview video URL format");
        }
    }

    private boolean isValidUrl(String url) {
        return StringUtils.hasText(url) && URL_PATTERN.matcher(url).matches();
    }
}