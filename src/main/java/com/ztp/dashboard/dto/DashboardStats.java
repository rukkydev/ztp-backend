package com.ztp.dashboard.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class DashboardStats {
    private final long totalUsers;
    private final long activeUsers;
    private final long suspendedUsers;
    private final long lockedAccounts;
}