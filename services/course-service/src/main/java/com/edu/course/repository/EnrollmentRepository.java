package com.edu.course.repository;

import com.edu.course.document.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {

    List<Enrollment> findByUserId(String userId);

    List<Enrollment> findByCourseId(String courseId);

    Optional<Enrollment> findByUserIdAndCourseId(String userId, String courseId);

    Page<Enrollment> findByUserId(String userId, Pageable pageable);

    Page<Enrollment> findByUserIdAndStatus(String userId, String status, Pageable pageable);

    long countByCourseId(String courseId);

    long countByUserIdAndStatus(String userId, String status);

    @Query("{ 'enrolledAt': { $gte: ?0, $lte: ?1 } }")
    long countByEnrolledAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'status': 'COMPLETED', 'completedAt': { $gte: ?0, $lte: ?1 } }")
    long countCompletedBetween(LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByUserIdAndCourseId(String userId, String courseId);
}