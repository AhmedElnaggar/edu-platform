package com.edu.user.repository;

import com.edu.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(UUID userId);

    Optional<UserProfile> findByDisplayName(String displayName);

    List<UserProfile> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT u FROM UserProfile u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<UserProfile> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT u FROM UserProfile u WHERE u.profileVisibility = 'PUBLIC'")
    Page<UserProfile> findPublicProfiles(Pageable pageable);

    boolean existsByUserId(UUID userId);

    boolean existsByDisplayName(String displayName);

    @Query("SELECT COUNT(u) FROM UserProfile u WHERE u.createdAt >= CURRENT_DATE")
    long countTodaysRegistrations();
}