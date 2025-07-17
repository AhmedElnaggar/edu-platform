package com.edu.auth.service;

import com.edu.auth.dto.LoginRequest;
import com.edu.auth.dto.LoginResponse;
import com.edu.auth.dto.RegisterRequest;
import com.edu.auth.entity.Role;
import com.edu.auth.entity.User;
import com.edu.auth.exception.AuthenticationException;
import com.edu.auth.exception.InvalidPasswordException;
import com.edu.auth.exception.InvalidTokenException;
import com.edu.auth.exception.RegistrationException;
import com.edu.auth.repository.RoleRepository;
import com.edu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.email-verification.token-expiration-hours:24}")
    private int emailVerificationTokenExpirationHours;

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

        // Validate password security requirements
        validatePasswordSecurity(request.getPassword());

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
                .emailVerified(false)
                .emailVerificationToken(generateVerificationToken())
                .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(emailVerificationTokenExpirationHours))
                .enabled(false) // Will be enabled after email verification
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(Set.of(studentRole))
                .build();

        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

/*        // Create user profile in user service
        try {
            CreateUserProfileRequest profileRequest = CreateUserProfileRequest.builder()
                    .userId(savedUser.getId())
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .role(UserRole.STUDENT.name())
                    .build();

            userServiceClient.createUserProfile(profileRequest);
        } catch (Exception e) {
            log.error("Failed to create user profile for user: {}", savedUser.getId(), e);
            // Rollback user creation in auth service
            userRepository.delete(savedUser);
            throw new RuntimeException("Failed to create user profile: " + e.getMessage());
        }

        // Send verification email
        emailService.sendVerificationEmail(savedUser);*/


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


    public LoginResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Email verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);

        User verifiedUser = userRepository.save(user);

        // Generate JWT token for verified user
        String jwtToken = jwtService.generateAccessToken(verifiedUser);
        String refreshToken = jwtService.generateRefreshToken(verifiedUser);

        return LoginResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .emailVerified(true)
                        .roles(user.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet()))
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
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private void validatePasswordSecurity(String password) {
        if (password.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new InvalidPasswordException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new InvalidPasswordException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Password must contain at least one number");
        }

        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new InvalidPasswordException("Password must contain at least one special character");
        }
    }
}