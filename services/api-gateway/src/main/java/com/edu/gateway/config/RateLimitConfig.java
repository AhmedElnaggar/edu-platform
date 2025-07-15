package com.edu.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    @Primary
    public RedisRateLimiter redisRateLimiter() {
        // Parameters: replenishRate, burstCapacity, requestedTokens
        // replenishRate: tokens per second
        // burstCapacity: maximum tokens in bucket
        // requestedTokens: tokens required per request
        return new RedisRateLimiter(10, 20, 1);
    }

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Try to get user ID from JWT token (added by AuthenticationFilter)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId);
            }

            // Fallback to IP address for unauthenticated requests
            String clientIp = "unknown";
            if (exchange.getRequest().getRemoteAddress() != null) {
                clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }

            return Mono.just("ip:" + clientIp);
        };
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = "unknown";
            if (exchange.getRequest().getRemoteAddress() != null) {
                clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }
            return Mono.just(clientIp);
        };
    }

    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            return Mono.just(path);
        };
    }

    // Different rate limiters for different scenarios
    @Bean
    public RedisRateLimiter authRateLimiter() {
        // More restrictive for auth endpoints
        return new RedisRateLimiter(5, 10, 1);
    }

    @Bean
    public RedisRateLimiter publicRateLimiter() {
        // More lenient for public endpoints
        return new RedisRateLimiter(20, 40, 1);
    }
}