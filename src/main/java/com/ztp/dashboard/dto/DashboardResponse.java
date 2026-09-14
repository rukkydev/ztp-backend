package com.ztp.dashboard.dto;

import com.ztp.audit.dto.AuditLogResponse;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardResponse {
    private final DashboardStats stats;
    private final Object resources;         // not built yet -- always null
    private final Object detectionTimeline; // not built yet -- always null
    private final List<AuditLogResponse> recentActivity;
}