package com.edu.course.repository;

import com.edu.course.document.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    // Find by instructor
    List<Course> findByInstructorId(String instructorId);

    // Find by category
    List<Course> findByCategory(String category);

    // Find active courses
    Page<Course> findByActiveTrue(Pageable pageable);

    // Find published courses
    Page<Course> findByActiveTrueAndStatus(String status, Pageable pageable);

    // Search courses by text (title, description, tags)
    @Query("{ $text: { $search: ?0 }, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> searchCourses(String searchTerm, Pageable pageable);

    // Find by tags
    @Query("{ 'tags': { $in: ?0 }, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> findByTagsIn(List<String> tags, Pageable pageable);

    // Find by price range
    @Query("{ 'price': { $gte: ?0, $lte: ?1 }, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Find by difficulty
    @Query("{ 'difficulty': ?0, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> findByDifficulty(String difficulty, Pageable pageable);

    // Find by rating
    @Query("{ 'rating': { $gte: ?0 }, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> findByRatingGreaterThanEqual(Double minRating, Pageable pageable);

    // Find by language
    @Query("{ 'language': ?0, 'active': true, 'status': 'PUBLISHED' }")
    Page<Course> findByLanguage(String language, Pageable pageable);

    // Count methods
    long countByInstructorId(String instructorId);
    long countByStatus(String status);
    long countByInstructorIdAndStatus(String instructorId, String status);

    // Find featured courses (high rating, many enrollments)
    @Query("{ 'active': true, 'status': 'PUBLISHED', 'rating': { $gte: 4.0 }, 'currentEnrollments': { $gte: 100 } }")
    List<Course> findFeaturedCourses(Pageable pageable);

    // Find trending courses (recent high enrollments)
    @Query("{ 'active': true, 'status': 'PUBLISHED', 'createdAt': { $gte: ?0 } }")
    List<Course> findTrendingCourses(java.time.LocalDateTime since, Pageable pageable);

    // Find courses by instructor with status
    List<Course> findByInstructorIdAndStatus(String instructorId, String status);

    // Find courses by instructor with active status
    List<Course> findByInstructorIdAndActive(String instructorId, Boolean active);

    // Find by multiple criteria
    @Query("{ 'active': true, 'status': 'PUBLISHED', " +
            "'category': { $in: ?0 }, " +
            "'difficulty': { $in: ?1 }, " +
            "'language': { $in: ?2 }, " +
            "'price': { $gte: ?3, $lte: ?4 } }")
    Page<Course> findByMultipleCriteria(List<String> categories,
                                        List<String> difficulties,
                                        List<String> languages,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        Pageable pageable);

    // Custom aggregation queries
    @Query("{ $and: [ " +
            "{ 'active': true }, " +
            "{ 'status': 'PUBLISHED' }, " +
            "{ $or: [ " +
            "  { 'title': { $regex: ?0, $options: 'i' } }, " +
            "  { 'description': { $regex: ?0, $options: 'i' } }, " +
            "  { 'tags': { $in: [?0] } } " +
            "] } " +
            "] }")
    Page<Course> findByTitleOrDescriptionOrTagsContaining(String searchTerm, Pageable pageable);

    // Find courses with discounts
    @Query("{ 'active': true, 'status': 'PUBLISHED', 'discountPrice': { $exists: true, $ne: null }, 'discountExpiry': { $gte: ?0 } }")
    Page<Course> findCoursesWithActiveDiscounts(java.time.LocalDateTime now, Pageable pageable);

    // Find courses by enrollment count range
    @Query("{ 'active': true, 'status': 'PUBLISHED', 'currentEnrollments': { $gte: ?0, $lte: ?1 } }")
    Page<Course> findByEnrollmentCountBetween(Integer minEnrollments, Integer maxEnrollments, Pageable pageable);

    // Find courses by duration range
    @Query("{ 'active': true, 'status': 'PUBLISHED', 'duration': { $gte: ?0, $lte: ?1 } }")
    Page<Course> findByDurationBetween(Integer minDuration, Integer maxDuration, Pageable pageable);

    // Additional utility methods
    Optional<Course> findByIdAndInstructorId(String id, String instructorId);
    Optional<Course> findByIdAndActive(String id, Boolean active);

    // Count active courses
    long countByActiveTrue();

    // Find recently created courses
    @Query("{ 'active': true, 'status': 'PUBLISHED', 'createdAt': { $gte: ?0 } }")
    List<Course> findRecentCourses(java.time.LocalDateTime since, Pageable pageable);
}