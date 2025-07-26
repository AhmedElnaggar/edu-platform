package com.edu.auth.event;

import com.edu.auth.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PasswordResetRequestedEvent extends EmailEvent {
    private final User user;
    private final String resetToken;

    public PasswordResetRequestedEvent(Object source, User user, String resetToken) {
        super(source, user.getEmail(), "PASSWORD_RESET", null);
        this.user = user;
        this.resetToken = resetToken;
    }

    public PasswordResetRequestedEvent(User user, String resetToken) {
        this(user, user, resetToken);
    }
}
