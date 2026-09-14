package com.ztp.session;

import com.ztp.session.dto.SessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import com.ztp.audit.AuditLogService;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRegistry sessionRegistry;
	private final AuditLogService auditLogService;

    public List<SessionResponse> listActiveSessions() {
        return sessionRegistry.getAllPrincipals().stream()
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .map(info -> new SessionResponse(
                        info.getSessionId(),
                        info.getPrincipal().toString(),
                        info.getLastRequest()))
                .toList();
    }
	
	public List<SessionResponse> listMySessions(String email) {
		return sessionRegistry.getAllSessions(email, false).stream()
				.map(info -> new SessionResponse(info.getSessionId(), email, info.getLastRequest()))
				.toList();
	}
	

	public void revokeSession(String sessionId, HttpServletRequest httpRequest) {
    SessionInformation info = sessionRegistry.getSessionInformation(sessionId);
    if (info == null) {
        throw new IllegalArgumentException("Session not found");
    }
    String targetUser = info.getPrincipal().toString();
    info.expireNow();

    String actor = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();
    auditLogService.record("SESSION_TERMINATED", null, actor,
            "Revoked session for: " + targetUser, httpRequest);
}

public void revokeMySession(String email, String sessionId, HttpServletRequest httpRequest) {
    SessionInformation info = sessionRegistry.getSessionInformation(sessionId);
    if (info == null || !info.getPrincipal().toString().equals(email)) {
        throw new IllegalArgumentException("Session not found");
    }
    info.expireNow();

    auditLogService.record("SESSION_TERMINATED", null, email,
            "Signed out own session", httpRequest);
}

	public void revokeAllOtherSessions(String email, String currentSessionId) {
		sessionRegistry.getAllSessions(email, false).stream()
				.filter(info -> !info.getSessionId().equals(currentSessionId))
				.forEach(org.springframework.security.core.session.SessionInformation::expireNow);
	}
	
	public void revokeSessions(List<String> sessionIds, HttpServletRequest httpRequest) {
    sessionIds.forEach(id -> revokeSession(id, httpRequest));
}
	
	
}