package com.ztp.account.dto;

import com.ztp.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProfileResponse {
    private final String fullName;
    private final String email;
    private final String phone;
    private final String jobTitle;
    private final String department;
    private final String role;
    private final LocalDateTime memberSince;
    private final LocalDateTime lastLogin;
    private final boolean twoFactorEnabled;
	private final String avatarUrl;
	
    public ProfileResponse(User user) {
        this.fullName = user.getUsername();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.jobTitle = user.getJobTitle();
        this.department = user.getDepartment();
        this.role = user.getRole().getName();
        this.memberSince = user.getCreatedAt();
        this.lastLogin = user.getLastLogin();
        this.twoFactorEnabled = user.isTwoFactorEnabled();
		this.avatarUrl = user.getAvatarUrl();
    }
}