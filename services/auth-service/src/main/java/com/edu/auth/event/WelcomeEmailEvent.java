package com.edu.auth.event;

import com.edu.auth.entity.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class WelcomeEmailEvent extends EmailEvent {
    private final User user;

    public WelcomeEmailEvent(Object source, User user) {
        super(source, user.getEmail(), "WELCOME", null);
        this.user = user;
    }

    // Convenience constructor
    public WelcomeEmailEvent(User user) {
        this(user, user);
    }
}
