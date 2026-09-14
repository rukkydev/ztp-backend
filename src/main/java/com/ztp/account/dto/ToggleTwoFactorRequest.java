package com.ztp.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToggleTwoFactorRequest {
    @NotBlank
    private String password;

    private boolean enabled;
}