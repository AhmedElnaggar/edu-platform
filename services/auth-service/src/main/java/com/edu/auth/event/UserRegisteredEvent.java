package com.edu.auth.event;

import com.edu.auth.entity.User;
import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserRegisteredEvent extends ApplicationEvent {
    private final User user;
    private final String eventType = "USER_REGISTERED";

    public UserRegisteredEvent(Object source, User user) {
        super(source);
        this.user = user;
    }

    // Convenience constructor
    public UserRegisteredEvent(User user) {
        super(user);
        this.user = user;
    }
}