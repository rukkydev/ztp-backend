package com.ztp.account;

import com.ztp.account.dto.AccountOverviewResponse;
import com.ztp.account.dto.ProfileResponse;
import com.ztp.account.recovery.RecoveryPhraseService;
import com.ztp.common.ApiResponse;
import com.ztp.device.DeviceService;
import com.ztp.notification.inapp.NotificationService;
import com.ztp.session.SessionService;
import com.ztp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountOverviewController {

    private final AccountService accountService;
    private final DeviceService deviceService;
    private final SessionService sessionService;
    private final NotificationService notificationService;
    private final RecoveryPhraseService recoveryPhraseService;
    private final UserRepository userRepository;

    @GetMapping("/overview")
    public ApiResponse<AccountOverviewResponse> overview() {
        Long userId = currentUserId();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        ProfileResponse profile = accountService.getMyProfile();
        int deviceCount = deviceService.listMyDevices(userId).size();
        int sessionCount = sessionService.listMySessions(email).size();
        long unread = notificationService.unreadCount(userId);
        boolean hasPhrase = recoveryPhraseService.hasRecoveryPhrase(userId);

        return ApiResponse.success("Account overview retrieved",
                new AccountOverviewResponse(profile, deviceCount, sessionCount, unread, hasPhrase));
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}