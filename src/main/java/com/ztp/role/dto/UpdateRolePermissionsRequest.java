package com.ztp.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateRolePermissionsRequest {

    @NotNull(message = "permissionIds is required")
    private Set<Long> permissionIds;
}