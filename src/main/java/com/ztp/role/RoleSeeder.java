package com.ztp.role;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
	

    @Override
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            return; // already seeded
        }

        Permission userView   = createPermission("USER_VIEW", "View user accounts");
        Permission userUnlock = createPermission("USER_UNLOCK", "Unlock a locked account");
        Permission userManage = createPermission("USER_MANAGE", "Create/update/delete user accounts");
        Permission roleManage = createPermission("ROLE_MANAGE", "Assign roles and manage permissions");
        Permission auditView  = createPermission("AUDIT_VIEW", "View audit logs");
		Permission settingsManage = createPermission("SETTINGS_MANAGE", "View and update platform settings");
		

        createRole("SUPER_ADMIN", "Full platform access",
                Set.of(userView, userUnlock, userManage, roleManage, auditView));

        createRole("SECURITY_NETWORK_ADMIN", "Manages users and devices, no role management",
                Set.of(userView, userUnlock, userManage));

        createRole("USER", "Standard authenticated user",
                Set.of());
    }

    private Permission createPermission(String name, String description) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }

    private void createRole(String name, String description, Set<Permission> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);
        roleRepository.save(role);
    }
}