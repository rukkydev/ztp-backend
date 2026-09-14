package com.ztp.notification;

import com.ztp.common.ApiResponse;
import com.ztp.notification.dto.NotificationPreferencesResponse;
import com.ztp.notification.dto.UpdateNotificationPreferencesRequest;
import com.ztp.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService service;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<NotificationPreferencesResponse> get() {
        return ApiResponse.success("Notification preferences retrieved", service.getMyPreferences(currentUserId()));
    }

    @PatchMapping
    public ApiResponse<NotificationPreferencesResponse> update(@Valid @RequestBody UpdateNotificationPreferencesRequest request) {
        return ApiResponse.success("Notification preferences updated", service.updateMyPreferences(currentUserId(), request));
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}