package com.edu.course.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class HeaderBasedAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extract user information from headers set by API Gateway
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String userRole = request.getHeader("X-User-Role");

        System.out.println("🔍 Header-based auth - UserId: " + userId + ", Username: " + username + ", Role: " + userRole);

        // If user information is present in headers, authenticate the user
        if (userId != null && !userId.isEmpty() && username != null && !username.isEmpty()) {

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            // Add role to authorities if present
            if (userRole != null && !userRole.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole));
            }

            // Create authentication token
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // Add custom details
            authentication.setDetails(new CustomUserDetails(userId, username, userRole));

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("✅ User authenticated via headers: " + username + " with role: " + userRole);
        }

        filterChain.doFilter(request, response);
    }

    // Custom user details class
    public static class CustomUserDetails {
        private final String userId;
        private final String username;
        private final String role;

        public CustomUserDetails(String userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}