package com.ztp.role.dto;

import com.ztp.role.Permission;
import lombok.Getter;

@Getter
public class PermissionResponse {
    private final Long id;
    private final String name;
    private final String description;

    public PermissionResponse(Permission permission) {
        this.id = permission.getId();
        this.name = permission.getName();
        this.description = permission.getDescription();
    }
}