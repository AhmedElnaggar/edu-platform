package com.edu.gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserRole(String token) {
        try {
            Claims claims = extractAllClaims(token);
            // Try different possible role claim names
            Object roles = claims.get("roles");
            if (roles != null) {
                if (roles instanceof List) {
                    List<?> roleList = (List<?>) roles;
                    if (!roleList.isEmpty()) {
                        return roleList.get(0).toString();
                    }
                } else {
                    return roles.toString();
                }
            }

            // Try other common role claim names
            Object role = claims.get("role");
            if (role != null) {
                return role.toString();
            }

            Object authorities = claims.get("authorities");
            if (authorities != null) {
                if (authorities instanceof List) {
                    List<?> authList = (List<?>) authorities;
                    if (!authList.isEmpty()) {
                        return authList.get(0).toString();
                    }
                } else {
                    return authorities.toString();
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error extracting role from token: " + e.getMessage());
            return null;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean isInvalid(String token) {
        try {
            // Check if token is null or empty
            if (token == null || token.trim().isEmpty()) {
                return true;
            }

            // Parse and validate token
            extractAllClaims(token);

            // Check if token is expired
            return isTokenExpired(token);

        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("JWT validation error: " + e.getMessage());
            return true;
        }
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Helper method to check if JWT format is correct
    public boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // JWT should have exactly 3 parts separated by dots
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }
}