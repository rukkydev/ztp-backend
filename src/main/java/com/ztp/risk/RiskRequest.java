package com.ztp.risk;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RiskRequest {
    private final Long userId;
    private final String eventType;
    private final boolean isNewDevice;
    private final boolean isNewIp;
    private final boolean loginHourUnusual;
    private final int failedLoginsLast10Min;
    private final int twoFactorFailuresLast10Min;
    private final String deviceId;
    private final String browser;
    private final String operatingSystem;
    private final String ipAddress;
}