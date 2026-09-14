package com.ztp.session;

import com.ztp.common.ApiResponse;
import com.ztp.session.dto.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account/sessions")
@RequiredArgsConstructor
public class AccountSessionController {

    private final SessionService sessionService;

    @GetMapping
    public ApiResponse<List<SessionResponse>> mySessions() {
        return ApiResponse.success("Sessions retrieved", sessionService.listMySessions(currentEmail()));
    }

    @DeleteMapping("/{sessionId}")
public ApiResponse<Void> revokeOne(@PathVariable String sessionId, HttpServletRequest httpRequest) {
    sessionService.revokeMySession(currentEmail(), sessionId, httpRequest);
    return ApiResponse.success("Session revoked", null);
}

    @DeleteMapping
    public ApiResponse<Void> revokeAllOthers(HttpServletRequest httpRequest) {
        String currentSessionId = httpRequest.getSession(false) != null
                ? httpRequest.getSession(false).getId()
                : null;
        sessionService.revokeAllOtherSessions(currentEmail(), currentSessionId);
        return ApiResponse.success("Other sessions signed out", null);
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}