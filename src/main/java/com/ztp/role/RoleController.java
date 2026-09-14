package com.ztp.role;

import com.ztp.common.ApiResponse;
import com.ztp.role.dto.RoleResponse;
import com.ztp.role.dto.UpdateRolePermissionsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.success("Roles retrieved", roleService.listRoles());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<RoleResponse> updateRolePermissions(@PathVariable Long id,
                                                             @Valid @RequestBody UpdateRolePermissionsRequest request) {
        RoleResponse response = roleService.updateRolePermissions(id, request);
        return ApiResponse.success("Role permissions updated", response);
    }
}