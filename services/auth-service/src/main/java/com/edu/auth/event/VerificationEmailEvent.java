package com.edu.auth.event;

import com.edu.auth.entity.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class VerificationEmailEvent extends EmailEvent {
    private final User user;

    public VerificationEmailEvent(Object source, User user) {
        super(source, user.getEmail(), "EMAIL_VERIFICATION",
                Map.of(
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "verificationToken", user.getEmailVerificationToken(),
                        "userId", user.getId()
                ));
        this.user = user;
    }

    // Convenience constructor
    public VerificationEmailEvent(User user) {
        this(user, user);
    }
}
