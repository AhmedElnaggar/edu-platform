package com.edu.auth.service;

import com.edu.auth.dto.LoginRequest;
import com.edu.auth.dto.LoginResponse;
import com.edu.auth.dto.RegisterRequest;
import com.edu.auth.entity.Role;
import com.edu.auth.entity.User;
import com.edu.auth.exception.AuthenticationException;
import com.edu.auth.exception.RegistrationException;
import com.edu.auth.repository.RoleRepository;
import com.edu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        log.info("Authentication attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsernameWithRoles(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed authentication attempt for username: {}", request.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        // Update last activity
        userRepository.updateLastActivity(user.getId(), LocalDateTime.now());

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Successful authentication for username: {}", request.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000) // Convert to seconds
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet()))
                        .lastLogin(LocalDateTime.now())
                        .build())
                .build();
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        // Validate unique constraints
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RegistrationException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RegistrationException("Email already exists");
        }

        // Get default role
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RegistrationException("Default role not found"));

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(Set.of(studentRole))
                .build();

        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Successful registration for username: {}", request.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(Set.of("STUDENT"))
                        .lastLogin(LocalDateTime.now())
                        .build())
                .build();
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtService.getUsernameFromToken(token);
    }
}