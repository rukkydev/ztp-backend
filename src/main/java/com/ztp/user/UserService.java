package com.ztp.user;

import com.ztp.audit.AuditLogService;
import com.ztp.role.Role;
import com.ztp.role.RoleRepository;
import com.ztp.user.dto.AdminUserResponse;
import com.ztp.user.dto.RegisterRequest;
import com.ztp.user.dto.CreateUserRequest;
import com.ztp.user.dto.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
	

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role not found -- was RoleSeeder run?"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(defaultRole);

        return userRepository.save(user);
    }

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::new)
                .toList();
    }

    public com.ztp.common.PagedResponse<AdminUserResponse> searchUsers(
            int page, int pageSize, String sort, String dir, String q, String status) {
        Boolean enabled = null;
        if ("Active".equalsIgnoreCase(status)) enabled = true;
        else if ("Suspended".equalsIgnoreCase(status)) enabled = false;

        String sortProp = (sort != null && !sort.isBlank()) ? sort : "id";
        // Validate sort property against known entity properties to prevent SQL/property injection
        if (!java.util.Set.of("id", "username", "email", "lastLogin", "createdAt", "department", "jobTitle").contains(sortProp)) {
            sortProp = "id";
        }

        org.springframework.data.domain.Sort.Direction direction = "asc".equalsIgnoreCase(dir) 
                ? org.springframework.data.domain.Sort.Direction.ASC 
                : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(0, page), Math.max(1, pageSize), org.springframework.data.domain.Sort.by(direction, sortProp));

        var resultPage = userRepository.searchUsers((q != null && !q.isBlank()) ? q.trim() : null, enabled, pageable);
        List<AdminUserResponse> responses = resultPage.getContent().stream()
                .map(AdminUserResponse::new)
                .toList();

        return new com.ztp.common.PagedResponse<>(responses, resultPage.getTotalElements(), page, pageSize);
    }



	public AdminUserResponse updateUser(Long userId, UpdateUserRequest request) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (request.getStatus() != null) {
        applyStatus(user, request.getStatus());
    }
    if (request.getUsername() != null) {
        user.setUsername(request.getUsername());
    }
    if (request.getEmail() != null) {
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        user.setEmail(request.getEmail());
    }
    if (request.getPhone() != null) {
        user.setPhone(request.getPhone());
    }
    if (request.getJobTitle() != null) {
        user.setJobTitle(request.getJobTitle());
    }
    if (request.getDepartment() != null) {
        user.setDepartment(request.getDepartment());
    }
    if (request.getRole() != null) {
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + request.getRole()));
        user.setRole(role);
    }

    AdminUserResponse response = new AdminUserResponse(userRepository.save(user));
	logAdminAction("USER_UPDATED", "Updated user: " + user.getUsername(), user.getId());

    return response;
}


    public List<AdminUserResponse> bulkSuspend(List<Long> ids) {
    List<User> users = userRepository.findAllById(ids);

    if (users.size() != ids.size()) {
        throw new IllegalArgumentException("One or more user IDs are invalid");
    }

    users.forEach(user -> user.setEnabled(false));
    List<User> savedUsers = userRepository.saveAll(users);

    ActorInfo actor = resolveCurrentActor();
    savedUsers.forEach(user ->
            logAdminAction("USER_STATUS_CHANGED", "Suspended user: " + user.getUsername(), user.getId(), actor));

    return savedUsers.stream()
            .map(AdminUserResponse::new)
            .toList();
}


    public AdminUserResponse unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        AdminUserResponse response = new AdminUserResponse(userRepository.save(user));

        logAdminAction("ACCOUNT_UNLOCKED", "Unlocked account: " + user.getUsername(), user.getId());

        return response;
    }

    private void applyStatus(User user, String status) {
        switch (status) {
            case "Active" -> user.setEnabled(true);
            case "Suspended" -> user.setEnabled(false);
            default -> throw new IllegalArgumentException("Invalid status: must be 'Active' or 'Suspended'");
        }
    }
	
	public AdminUserResponse createUser(CreateUserRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new IllegalArgumentException("Username is already taken");
    }
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("Email is already registered");
    }

    Role role = roleRepository.findByName(request.getRole())
            .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + request.getRole()));

    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(role);

    AdminUserResponse response = new AdminUserResponse(userRepository.save(user));

    logAdminAction("USER_CREATED", "Created user: " + user.getUsername() + " with role " + role.getName(), user.getId());

    return response;
}

public AdminUserResponse getUser(Long userId) {
    return userRepository.findById(userId)
            .map(AdminUserResponse::new)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
}

    /**
     * Logs an admin-initiated action, attributing it to whoever is
     * currently authenticated -- never to the target of the action.
     */
 
private record ActorInfo(Long id, String email) {}

private ActorInfo resolveCurrentActor() {
    String actorEmail = auditLogService.currentActorEmail();
    Long actorId = userRepository.findByEmail(actorEmail).map(User::getId).orElse(null);
    return new ActorInfo(actorId, actorEmail);
}

/**
 * Logs an admin-initiated action, attributing it to whoever is
 * currently authenticated -- never to the target of the action.
 */
private void logAdminAction(String eventType, String description, Long targetUserId) {
    logAdminAction(eventType, description, targetUserId, resolveCurrentActor());
}

/** Same as above, but for callers that already have the actor resolved
 *  (bulk operations) -- avoids re-looking up the same admin N times. */
private void logAdminAction(String eventType, String description, Long targetUserId, ActorInfo actor) {
    auditLogService.record(eventType, actor.id(), actor.email(), description, null, targetUserId);
}

}