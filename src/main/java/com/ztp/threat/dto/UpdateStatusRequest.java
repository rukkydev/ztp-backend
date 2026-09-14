package com.ztp.threat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {
    @NotBlank
    private String status;

    private String outcome; // CONFIRMED_THREAT | FALSE_POSITIVE
}