package com.ztp.session.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkSessionIdsRequest {

    @NotEmpty
    private List<String> ids;
}