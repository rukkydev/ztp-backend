package com.ztp.auth.dto;

import com.ztp.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private final UserResponse user;
    private final boolean deviceVerificationRequired;
    private final boolean twoFactorRequired;
}