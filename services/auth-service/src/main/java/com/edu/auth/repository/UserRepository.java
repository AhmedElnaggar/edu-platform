package com.edu.auth.repository;

import com.edu.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByPasswordResetToken(String token);

    @Query("SELECT u FROM User u WHERE u.passwordResetTokenExpiry < :now")
    List<User> findUsersWithExpiredResetTokens(@Param("now") LocalDateTime now);

    @Query("UPDATE User u SET u.passwordResetToken = null, u.passwordResetTokenExpiry = null WHERE u.passwordResetTokenExpiry < :now")
    void clearExpiredResetTokens(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Modifying
    @Query("UPDATE User u SET u.updatedAt = :timestamp WHERE u.id = :userId")
    void updateLastActivity(@Param("userId") UUID userId, @Param("timestamp") LocalDateTime timestamp);
}