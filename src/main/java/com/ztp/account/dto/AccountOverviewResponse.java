package com.ztp.account.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class AccountOverviewResponse {
    private final ProfileResponse profile;
    private final int deviceCount;
    private final int sessionCount;
    private final long unreadNotifications;
    private final boolean hasRecoveryPhrase;
}