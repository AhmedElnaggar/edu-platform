package com.edu.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("lb://auth-service"))

                // User Service Routes
                .route("user-service", r -> r.path("/users/**")
                        .uri("lb://user-service"))

                // Course Service Routes
                .route("course-service", r -> r.path("/courses/**")
                        .uri("lb://course-service"))

                // Health check routes
                .route("auth-health", r -> r.path("/auth/health")
                        .uri("lb://auth-service"))
                .route("user-health", r -> r.path("/users/health")
                        .uri("lb://user-service"))
                .route("course-health", r -> r.path("/courses/health")
                        .uri("lb://course-service"))

                .build();
    }
}