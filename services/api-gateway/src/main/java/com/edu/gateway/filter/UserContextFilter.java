/*
package com.edu.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class UserContextFilter implements GlobalFilter, Ordered {

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/actuator",
            "/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip processing for excluded paths
        if (shouldSkipProcessing(path)) {
            log.debug("Skipping user context processing for path: {}", path);
            return chain.filter(exchange);
        }

        // Get security context and extract user information
        return ReactiveSecurityContextHolder.getContext()
                .cast(org.springframework.security.core.context.SecurityContext.class)
                .map(securityContext -> securityContext.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .flatMap(jwtToken -> {
                    try {
                        // Extract user information from JWT
                        String userId = jwtToken.getToken().getClaimAsString("userId");
                        String email = jwtToken.getToken().getClaimAsString("email");
                        String firstName = jwtToken.getToken().getClaimAsString("firstName");
                        String lastName = jwtToken.getToken().getClaimAsString("lastName");
                        String username = jwtToken.getToken().getClaimAsString("sub");

                        // Extract roles
                        List<String> roles = jwtToken.getToken().getClaimAsStringList("roles");
                        String rolesHeader = roles != null ? String.join(",", roles) : "STUDENT";

                        // Generate request ID for tracing
                        String requestId = UUID.randomUUID().toString();

                        // Create modified request with user context headers
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-ID", userId != null ? userId : "")
                                .header("X-User-Email", email != null ? email : "")
                                .header("X-User-Username", username != null ? username : "")
                                .header("X-User-First-Name", firstName != null ? firstName : "")
                                .header("X-User-Last-Name", lastName != null ? lastName : "")
                                .header("X-User-Roles", rolesHeader)
                                .header("X-Request-ID", requestId)
                                .build();

                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(modifiedRequest)
                                .build();

                        log.info("User context added - User: {}, Roles: {}, RequestID: {}",
                                username, rolesHeader, requestId);

                        return chain.filter(modifiedExchange);

                    } catch (Exception e) {
                        log.warn("Error extracting user context from JWT: {}", e.getMessage());
                        return chain.filter(exchange);
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("No security context found, proceeding without user context headers");
                    return chain.filter(exchange);
                }));
    }

    private boolean shouldSkipProcessing(String path) {
        return EXCLUDED_PATHS.stream()
                .anyMatch(excludedPath -> path.startsWith(excludedPath));
    }

    @Override
    public int getOrder() {
        return 0; // Execute after authentication but before routing
    }
}*/
