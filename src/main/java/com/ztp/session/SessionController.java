package com.ztp.session;

import com.ztp.common.ApiResponse;
import com.ztp.session.dto.SessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ztp.session.dto.BulkSessionIdsRequest;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ApiResponse<List<SessionResponse>> listSessions() {
        return ApiResponse.success("Sessions retrieved", sessionService.listActiveSessions());
    }
	
	@DeleteMapping("/{sessionId}")
@PreAuthorize("hasAuthority('USER_MANAGE')")
public ApiResponse<Void> revokeSession(@PathVariable String sessionId, HttpServletRequest httpRequest) {
    sessionService.revokeSession(sessionId, httpRequest);
    return ApiResponse.success("Session revoked", null);
}
	
	@DeleteMapping("/bulk")
@PreAuthorize("hasAuthority('USER_MANAGE')")
public ApiResponse<Void> revokeBulk(@Valid @RequestBody BulkSessionIdsRequest request, HttpServletRequest httpRequest) {
    sessionService.revokeSessions(request.getIds(), httpRequest);
    return ApiResponse.success("Sessions revoked", null);
}
}