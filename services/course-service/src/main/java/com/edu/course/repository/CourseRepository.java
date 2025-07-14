package com.edu.course.repository;

import com.edu.course.document.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    List<Course> findByInstructorId(String instructorId);

    List<Course> findByCategory(String category);

    Page<Course> findByActiveTrue(Pageable pageable);

    Page<Course> findByActiveTrueAndStatus(String status, Pageable pageable);

    @Query("{ $text: { $search: ?0 }, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> searchCourses(String searchTerm, Pageable pageable);

    @Query("{ 'tags': { $in: ?0 }, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> findByTagsIn(List<String> tags, Pageable pageable);

    @Query("{ 'price': { $gte: ?0, $lte: ?1 }, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    @Query("{ 'difficulty': ?0, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> findByDifficulty(String difficulty, Pageable pageable);

    @Query("{ 'rating': { $gte: ?0 }, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> findByRatingGreaterThanEqual(Double minRating, Pageable pageable);

    long countByInstructorId(String instructorId);

    long countByStatus(String status);
}