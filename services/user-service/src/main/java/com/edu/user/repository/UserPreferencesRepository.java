package com.edu.user.repository;

import com.edu.user.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {

    Optional<UserPreferences> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM UserPreferences up WHERE up.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(up) FROM UserPreferences up WHERE up.emailNotifications = true")
    long countUsersWithEmailNotifications();

    @Query("SELECT COUNT(up) FROM UserPreferences up WHERE up.pushNotifications = true")
    long countUsersWithPushNotifications();
}