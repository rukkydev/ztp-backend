package com.ztp.threat.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkResolutionRequest {

    @NotEmpty(message = "ids must not be empty")
    private List<Long> ids;

    private String outcome; // CONFIRMED_THREAT | FALSE_POSITIVE
}
