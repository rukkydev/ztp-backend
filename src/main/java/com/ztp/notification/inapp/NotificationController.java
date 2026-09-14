package com.ztp.notification.inapp;

import com.ztp.common.ApiResponse;
import com.ztp.notification.inapp.dto.NotificationResponse;
import com.ztp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> list() {
        return ApiResponse.success("Notifications retrieved", service.listMyNotifications(currentUserId()));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.success("Unread count retrieved", Map.of("count", service.unreadCount(currentUserId())));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        service.markRead(currentUserId(), id);
        return ApiResponse.success("Notification marked as read", null);
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        service.markAllRead(currentUserId());
        return ApiResponse.success("All notifications marked as read", null);
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }
}