package com.ztp.dashboard;

import com.ztp.audit.AuditLogRepository;
import com.ztp.audit.dto.AuditLogResponse;
import com.ztp.dashboard.dto.DashboardResponse;
import com.ztp.dashboard.dto.DashboardStats;
import com.ztp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int RECENT_ACTIVITY_LIMIT = 10;

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardResponse getDashboard() {
        var allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(u -> u.isEnabled() && !u.isAccountLocked()).count();
        long suspendedUsers = allUsers.stream().filter(u -> !u.isEnabled()).count();
        long lockedAccounts = allUsers.stream().filter(u -> u.isAccountLocked()).count();

        DashboardStats stats = new DashboardStats(totalUsers, activeUsers, suspendedUsers, lockedAccounts);

        var recentActivity = auditLogRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_ACTIVITY_LIMIT))
                .stream()
                .map(AuditLogResponse::new)
                .toList();

        return new DashboardResponse(stats, null, null, recentActivity);
    }
}