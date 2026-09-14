package com.ztp.role;

import com.ztp.audit.AuditLogService;
import com.ztp.role.dto.RoleResponse;
import com.ztp.role.dto.UpdateRolePermissionsRequest;
import com.ztp.user.User;
import com.ztp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::new)
                .toList();
    }

    public RoleResponse updateRolePermissions(Long roleId, UpdateRolePermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));

        if (permissions.size() != request.getPermissionIds().size()) {
            throw new IllegalArgumentException("One or more permission IDs are invalid");
        }

        role.setPermissions(permissions);
        Role saved = roleRepository.save(role);

        String actorEmail = auditLogService.currentActorEmail();
        Long actorId = userRepository.findByEmail(actorEmail).map(User::getId).orElse(null);
        auditLogService.record("ROLE_PERMISSIONS_UPDATED", actorId, actorEmail,
                "Updated permissions for role: " + role.getName(), null);

        return new RoleResponse(saved);
    }
}