package com.edu.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.edu.gateway.service.JwtService;

import java.util.List;
import java.util.function.Predicate;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        System.out.println("🔐 AuthenticationFilter processing: " + request.getMethod() + " " + path);

        // Check if the request is for a secured endpoint
        if (isSecured.test(request)) {
            System.out.println("🔒 Secured endpoint detected, checking authentication...");

            // Check if Authorization header is present
            if (this.isAuthMissing(request)) {
                System.out.println("❌ Authorization header is missing");
                return this.onError(exchange, "Authorization header is missing in request", HttpStatus.UNAUTHORIZED);
            }

            final String token = this.getAuthHeader(request);
            System.out.println("🎫 Token extracted: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));

            // Validate JWT token
            if (jwtService.isInvalid(token)) {
                System.out.println("❌ Token is invalid");
                return this.onError(exchange, "Authorization header is invalid", HttpStatus.UNAUTHORIZED);
            }

            System.out.println("✅ Token is valid, proceeding with request");

            // ✅ FIXED: Extract user information and create mutated exchange
            ServerWebExchange mutatedExchange = populateRequestWithHeaders(exchange, token);
            return chain.filter(mutatedExchange); // ✅ Pass mutated exchange
        } else {
            System.out.println("🔓 Open endpoint, skipping authentication");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String body = "{\"error\":\"" + err + "\"}";
        System.out.println("🚨 Returning error: " + httpStatus + " - " + err);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private String getAuthHeader(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    // ✅ FIXED: Return mutated exchange instead of void
    private ServerWebExchange populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        try {
            String userId = jwtService.extractUserId(token);
            String userRole = jwtService.extractUserRole(token);
            String username = jwtService.extractUsername(token);

            System.out.println("👤 Extracted user info - ID: " + userId + ", Username: " + username + ", Role: " + userRole);

            // ✅ Create mutated request with headers
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-User-Role", userRole != null ? userRole : "")
                    .header("X-Username", username != null ? username : "")
                    .build();

            // ✅ Return mutated exchange with new request
            return exchange.mutate().request(mutatedRequest).build();

        } catch (Exception e) {
            System.err.println("❌ Error extracting user info from token: " + e.getMessage());
            e.printStackTrace();
            return exchange; // Return original exchange on error
        }
    }

    // Define which endpoints are secured (require authentication)
    private final Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();

        // List of open endpoints that don't require authentication
        List<String> openApiEndpoints = List.of(
                "/auth/register",
                "/auth/login",
                "/auth/verify-email",
                "/auth/forgot-password",
                "/auth/reset-password",
                "/auth/refresh",
                "/api/v1/courses", // GET only for public course browsing
                "/api/v1/courses/search",
                "/api/v1/courses/category",
                "/api/v1/courses/featured",
                "/api/v1/courses/trending",
                "/actuator",
                "/health",
                "/swagger-ui",
                "/api-docs",
                "/debug"
        );

        // Check if the current path matches any open endpoint
        boolean isOpenEndpoint = openApiEndpoints.stream()
                .anyMatch(openPath -> path.contains(openPath));

        System.out.println("🔍 Path: " + path + " - Open endpoint: " + isOpenEndpoint);

        return !isOpenEndpoint;
    };
}