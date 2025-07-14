package com.edu.user.health;

import com.edu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("userDatabase")
@RequiredArgsConstructor
public class UserServiceHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    @Override
    public Health health() {
        try {
            long userCount = userRepository.count();
            long todaysRegistrations = userRepository.countTodaysRegistrations();

            return Health.up()
                    .withDetail("totalUsers", userCount)
                    .withDetail("todaysRegistrations", todaysRegistrations)
                    .withDetail("status", "Database connection healthy")
                    .withDetail("service", "user-service")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "Database connection failed")
                    .withDetail("service", "user-service")
                    .build();
        }
    }
}