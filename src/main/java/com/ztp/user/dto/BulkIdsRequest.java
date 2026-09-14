package com.ztp.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkIdsRequest {

    @NotEmpty(message = "ids must not be empty")
    private List<Long> ids;
}