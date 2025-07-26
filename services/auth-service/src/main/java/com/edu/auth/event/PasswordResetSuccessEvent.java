package com.edu.auth.event;

import com.edu.auth.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PasswordResetSuccessEvent extends EmailEvent {
    private final User user;

    public PasswordResetSuccessEvent(Object source, User user) {
        super(source, user.getEmail(), "PASSWORD_RESET_SUCCESS", null);
        this.user = user;
    }

    public PasswordResetSuccessEvent(User user) {
        this(user, user);
    }
}
