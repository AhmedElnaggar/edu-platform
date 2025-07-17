package com.edu.course.utils;

import com.edu.course.exception.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class SecurityUtils {

    // Headers injected by API Gateway after authentication
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Get current user ID from request headers (injected by API Gateway)
     */
    public String getCurrentUserId() {
        String userId = getHeaderValue(USER_ID_HEADER);
        if (!StringUtils.hasText(userId)) {
            throw new UnauthorizedAccessException("User ID not found in request headers");
        }
        return userId;
    }

    /**
     * Get current user email from request headers
     */
    public String getCurrentUserEmail() {
        return getHeaderValue(USER_EMAIL_HEADER);
    }

    /**
     * Get current user name from request headers
     */
    public String getCurrentUserName() {
        return getHeaderValue(USER_NAME_HEADER);
    }

    /**
     * Get current user roles from request headers
     */
    public List<String> getCurrentUserRoles() {
        String rolesHeader = getHeaderValue(USER_ROLES_HEADER);
        if (StringUtils.hasText(rolesHeader)) {
            return Arrays.asList(rolesHeader.split(","));
        }
        return List.of();
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(String role) {
        List<String> roles = getCurrentUserRoles();
        return roles.contains(role) || roles.contains("ROLE_" + role);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        List<String> userRoles = getCurrentUserRoles();
        return Arrays.stream(roles)
                .anyMatch(role -> userRoles.contains(role) || userRoles.contains("ROLE_" + role));
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is instructor
     */
    public boolean isInstructor() {
        return hasRole("INSTRUCTOR");
    }

    /**
     * Check if current user is student
     */
    public boolean isStudent() {
        return hasRole("STUDENT");
    }

    /**
     * Check if current user owns the resource (same user ID)
     */
    public boolean isOwner(String resourceOwnerId) {
        String currentUserId = getCurrentUserId();
        return currentUserId.equals(resourceOwnerId);
    }

    /**
     * Check if current user can access resource (owner or admin)
     */
    public boolean canAccessResource(String resourceOwnerId) {
        return isOwner(resourceOwnerId) || isAdmin();
    }

    /**
     * Get authorization header from current request (for downstream service calls)
     */
    public String getAuthHeader() {
        return getHeaderValue(AUTHORIZATION_HEADER);
    }

    /**
     * Validate that required user information is present
     */
    public void validateUserContext() {
        String userId = getHeaderValue(USER_ID_HEADER);
        if (!StringUtils.hasText(userId)) {
            throw new UnauthorizedAccessException("Missing user context in request headers");
        }
    }

    /**
     * Get optional user ID (doesn't throw exception if not present)
     */
    public Optional<String> getCurrentUserIdOptional() {
        try {
            String userId = getHeaderValue(USER_ID_HEADER);
            return StringUtils.hasText(userId) ? Optional.of(userId) : Optional.empty();
        } catch (Exception e) {
            log.debug("Unable to get user ID from headers", e);
            return Optional.empty();
        }
    }

    /**
     * Check if user is authenticated (has user ID in headers)
     */
    public boolean isAuthenticated() {
        return getCurrentUserIdOptional().isPresent();
    }

    /**
     * Get user context information for logging
     */
    public String getUserContext() {
        try {
            String userId = getCurrentUserId();
            String email = getCurrentUserEmail();
            List<String> roles = getCurrentUserRoles();
            return String.format("User{id='%s', email='%s', roles=%s}",
                    userId, email, roles);
        } catch (Exception e) {
            return "User{anonymous}";
        }
    }

    /**
     * Authorize instructor access to course
     */
    public void authorizeInstructorAccess(String courseInstructorId) {
        String currentUserId = getCurrentUserId();

        if (!isAdmin() && !currentUserId.equals(courseInstructorId)) {
            throw new UnauthorizedAccessException("Access denied. Only course instructor or admin can perform this action");
        }
    }

    /**
     * Authorize admin access
     */
    public void authorizeAdminAccess() {
        if (!isAdmin()) {
            throw new UnauthorizedAccessException("Access denied. Admin role required");
        }
    }

    /**
     * Authorize instructor or admin access
     */
    public void authorizeInstructorOrAdminAccess() {
        if (!isInstructor() && !isAdmin()) {
            throw new UnauthorizedAccessException("Access denied. Instructor or admin role required");
        }
    }

    /**
     * Extract header value from current request
     */
    private String getHeaderValue(String headerName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(headerName);
        }
        return null;
    }

    /**
     * Get all user headers for debugging
     */
    public String getAllUserHeaders() {
        return String.format("Headers{userId='%s', email='%s', roles='%s', name='%s'}",
                getHeaderValue(USER_ID_HEADER),
                getHeaderValue(USER_EMAIL_HEADER),
                getHeaderValue(USER_ROLES_HEADER),
                getHeaderValue(USER_NAME_HEADER));
    }
}