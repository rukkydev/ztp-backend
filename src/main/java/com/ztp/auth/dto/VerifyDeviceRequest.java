package com.ztp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyDeviceRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String code;

    private boolean rememberDevice;
}