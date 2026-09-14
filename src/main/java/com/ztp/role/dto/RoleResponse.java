package com.ztp.role.dto;

import com.ztp.role.Role;
import lombok.Getter;

import java.util.List;

@Getter
public class RoleResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final List<PermissionResponse> permissions;

    public RoleResponse(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.permissions = role.getPermissions().stream()
                .map(PermissionResponse::new)
                .toList();
    }
}