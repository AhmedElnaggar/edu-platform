package com.edu.user.config;

import com.edu.user.security.HeaderBasedAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private HeaderBasedAuthenticationFilter headerBasedAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/actuator/**", "/health", "/error").permitAll()  // Allow health checks
                        .requestMatchers("/users/**").authenticated()  // Require authentication for user endpoints
                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerBasedAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}