package com.ztp.user.dto;

import com.ztp.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminUserResponse {
    private final Long id;
    private final String username;
    private final String email;
    private final String role;
    private final boolean enabled;
    private final boolean accountLocked;
    private final LocalDateTime createdAt;

    public AdminUserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().getName();
        this.enabled = user.isEnabled();
        this.accountLocked = user.isAccountLocked();
        this.createdAt = user.getCreatedAt();
    }
}