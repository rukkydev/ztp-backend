package com.ztp.user;

import com.ztp.role.Role;
import com.ztp.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class AdminUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@ztp.local}")
    private String adminEmail;

    @Value("${app.seed.admin-username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin-password:Admin@123!}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) { return; }
        Role superAdmin = roleRepository.findByName("SUPER_ADMIN").orElse(null);
        if (superAdmin == null) {
            log.warn("AdminUserSeeder: SUPER_ADMIN role not found -- skipping.");
            return;
        }
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(superAdmin);
        admin.setEnabled(true);
        admin.setTwoFactorEnabled(false);
        admin.setAccountLocked(false);
        admin.setJobTitle("System Administrator");
        admin.setDepartment("IT");
        userRepository.save(admin);
        log.info("AdminUserSeeder: SUPER_ADMIN created email={} username={}.", adminEmail, adminUsername);
    }
}