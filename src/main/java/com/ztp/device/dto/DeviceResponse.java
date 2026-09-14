package com.ztp.device.dto;

import com.ztp.device.Device;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DeviceResponse {
    private final Long id;
    private final Long userId;
    private final String userAgent;
    private final String ipAddress;
    private final String status; // "Trusted" | "Blocked" | "Pending"
    private final LocalDateTime lastSeenAt;
    private final LocalDateTime createdAt;

    public DeviceResponse(Device device) {
        this.id = device.getId();
        this.userId = device.getUserId();
        this.userAgent = device.getUserAgent();
        this.ipAddress = device.getIpAddress();
        this.status = device.isBlocked() ? "Blocked" : (device.isTrusted() ? "Trusted" : "Pending");
        this.lastSeenAt = device.getLastSeenAt();
        this.createdAt = device.getCreatedAt();
    }
}