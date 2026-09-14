package com.ztp.device.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDeviceStatusRequest {

    @NotBlank
    private String status; // "Blocked" or "Trusted"
}