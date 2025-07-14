package com.edu.course.events;

import com.edu.course.document.Course;
import com.edu.course.document.Enrollment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String COURSE_CREATED_TOPIC = "course.created";
    private static final String COURSE_UPDATED_TOPIC = "course.updated";
    private static final String COURSE_PUBLISHED_TOPIC = "course.published";
    private static final String COURSE_DELETED_TOPIC = "course.deleted";
    private static final String COURSE_ENROLLED_TOPIC = "course.enrolled";
    private static final String COURSE_COMPLETED_TOPIC = "course.completed";
    private static final String COURSE_UNENROLLED_TOPIC = "course.unenrolled";

    public void publishCourseCreated(Course course) {
        try {
            Map<String, Object> event = createCourseEvent(course, "COURSE_CREATED");
            kafkaTemplate.send(COURSE_CREATED_TOPIC, course.getId(), event);
            log.info("Published course created event for courseId: {}", course.getId());
        } catch (Exception e) {
            log.error("Failed to publish course created event for courseId: {}", course.getId(), e);
        }
    }

    public void publishCourseUpdated(Course course) {
        try {
            Map<String, Object> event = createCourseEvent(course, "COURSE_UPDATED");
            kafkaTemplate.send(COURSE_UPDATED_TOPIC, course.getId(), event);
            log.info("Published course updated event for courseId: {}", course.getId());
        } catch (Exception e) {
            log.error("Failed to publish course updated event for courseId: {}", course.getId(), e);
        }
    }

    public void publishCoursePublished(Course course) {
        try {
            Map<String, Object> event = createCourseEvent(course, "COURSE_PUBLISHED");
            kafkaTemplate.send(COURSE_PUBLISHED_TOPIC, course.getId(), event);
            log.info("Published course published event for courseId: {}", course.getId());
        } catch (Exception e) {
            log.error("Failed to publish course published event for courseId: {}", course.getId(), e);
        }
    }

    public void publishCourseDeleted(String courseId, String instructorId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "COURSE_DELETED");
            event.put("courseId", courseId);
            event.put("instructorId", instructorId);
            event.put("timestamp", LocalDateTime.now());
            event.put("source", "course-service");

            kafkaTemplate.send(COURSE_DELETED_TOPIC, courseId, event);
            log.info("Published course deleted event for courseId: {}", courseId);
        } catch (Exception e) {
            log.error("Failed to publish course deleted event for courseId: {}", courseId, e);
        }
    }

    public void publishCourseEnrolled(Enrollment enrollment, Course course) {
        try {
            Map<String, Object> event = createEnrollmentEvent(enrollment, course, "COURSE_ENROLLED");
            kafkaTemplate.send(COURSE_ENROLLED_TOPIC, enrollment.getId(), event);
            log.info("Published course enrolled event for enrollmentId: {}", enrollment.getId());
        } catch (Exception e) {
            log.error("Failed to publish course enrolled event for enrollmentId: {}", enrollment.getId(), e);
        }
    }

    public void publishCourseCompleted(Enrollment enrollment, Course course) {
        try {
            Map<String, Object> event = createEnrollmentEvent(enrollment, course, "COURSE_COMPLETED");
            kafkaTemplate.send(COURSE_COMPLETED_TOPIC, enrollment.getId(), event);
            log.info("Published course completed event for enrollmentId: {}", enrollment.getId());
        } catch (Exception e) {
            log.error("Failed to publish course completed event for enrollmentId: {}", enrollment.getId(), e);
        }
    }

    public void publishCourseUnenrolled(Enrollment enrollment, Course course) {
        try {
            Map<String, Object> event = createEnrollmentEvent(enrollment, course, "COURSE_UNENROLLED");
            kafkaTemplate.send(COURSE_UNENROLLED_TOPIC, enrollment.getId(), event);
            log.info("Published course unenrolled event for enrollmentId: {}", enrollment.getId());
        } catch (Exception e) {
            log.error("Failed to publish course unenrolled event for enrollmentId: {}", enrollment.getId(), e);
        }
    }

    private Map<String, Object> createCourseEvent(Course course, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("courseId", course.getId());
        event.put("title", course.getTitle());
        event.put("instructorId", course.getInstructorId());
        event.put("category", course.getCategory());
        event.put("price", course.getPrice());
        event.put("status", course.getStatus());
        event.put("timestamp", LocalDateTime.now());
        event.put("source", "course-service");
        event.put("version", "1.0");

        return event;
    }

    private Map<String, Object> createEnrollmentEvent(Enrollment enrollment, Course course, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("enrollmentId", enrollment.getId());
        event.put("courseId", enrollment.getCourseId());
        event.put("userId", enrollment.getUserId());
        event.put("status", enrollment.getStatus());
        event.put("progress", enrollment.getProgress());
        event.put("timestamp", LocalDateTime.now());
        event.put("source", "course-service");

        // Course info
        if (course != null) {
            event.put("courseTitle", course.getTitle());
            event.put("instructorId", course.getInstructorId());
            event.put("coursePrice", course.getPrice());
        }

        return event;
    }
}