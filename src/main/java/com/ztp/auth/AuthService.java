package com.ztp.auth;

import com.ztp.audit.AuditLogService;
import com.ztp.auth.dto.LoginRequest;
import com.ztp.auth.dto.LoginResponse;
import com.ztp.user.User;
import com.ztp.user.UserRepository;
import com.ztp.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import com.ztp.auth.dto.TwoFactorVerifyRequest;
import com.ztp.auth.dto.VerifyDeviceRequest;
import com.ztp.auth.dto.ResendCodeRequest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AuditLogService auditLogService;
    private final com.ztp.otp.OtpService otpService;
	private final com.ztp.risk.RiskLogService riskLogService;
    private final com.ztp.device.DeviceService deviceService;
    private final org.springframework.security.core.session.SessionRegistry sessionRegistry;
	private final com.ztp.risk.RiskEvaluationClient riskClient;
	private final com.ztp.session.device.ActiveSessionDeviceRepository activeSessionDeviceRepository;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
            user.setFailedLoginAttempts(0);
            user.setLastLogin(java.time.LocalDateTime.now());
            userRepository.save(user);

            String deviceId = httpRequest.getHeader("X-Device-Id");

            if (deviceService.isBlocked(user.getId(), deviceId)) {
				String correlationId = java.util.UUID.randomUUID().toString();
				com.ztp.risk.RiskRequest riskRequest = new com.ztp.risk.RiskRequest(
						user.getId(), "LOGIN_BLOCKED_DEVICE", true, false, false, 0, 0,
						deviceId, null, null, resolveIpForRisk(httpRequest)
				);
				var riskResult = riskClient.evaluateWithMeta(riskRequest);
				riskLogService.record(correlationId, riskRequest, riskResult.getResponse(), "BLOCK", riskResult.isEvaluateReached());

				auditLogService.record("LOGIN_BLOCKED_DEVICE", user.getId(), user.getUsername(),
					"Login attempt from a blocked device", httpRequest);
				throw new IllegalArgumentException("This device has been blocked. Contact an administrator.");
			}

            if (!deviceService.isTrustedDevice(user.getId(), deviceId)) {
                deviceService.recordSighting(user.getId(), deviceId, httpRequest, false);
                otpService.issueCode(user.getId(), user.getEmail(), "DEVICE_VERIFICATION");
                auditLogService.record("DEVICE_VERIFICATION_CODE_SENT", user.getId(), user.getUsername(),
                    "Unrecognized device, verification code sent", httpRequest);
                return new LoginResponse(new UserResponse(user), true, false);
            }
			
			String correlationId = java.util.UUID.randomUUID().toString();

boolean isNewIp = auditLogService.computeIsNewIp(user.getId(), resolveIpForRisk(httpRequest));
boolean loginHourUnusual = auditLogService.computeLoginHourUnusual(user.getId());
int recentFailedLogins = auditLogService.countRecentFailedLogins(user.getId(), 10);
int recentTwoFactorFailures = auditLogService.countRecentTwoFactorFailures(user.getId(), 10);

com.ztp.risk.RiskRequest riskRequest = new com.ztp.risk.RiskRequest(
        user.getId(), "LOGIN_SUCCESS", false, isNewIp, loginHourUnusual,
        recentFailedLogins, recentTwoFactorFailures,
        deviceId, null, null, resolveIpForRisk(httpRequest)
);

var riskResult = riskClient.evaluateWithMeta(riskRequest);
com.ztp.risk.RiskResponse risk = riskResult.getResponse();

String enforcedAction = risk.getRecommendedAction();

riskLogService.record(correlationId, riskRequest, risk, enforcedAction, riskResult.isEvaluateReached());

auditLogService.record("RISK_EVALUATED", user.getId(), user.getUsername(),
        "score=" + risk.getRiskScore() + " level=" + risk.getRiskLevel() + " action=" + risk.getRecommendedAction(),
        httpRequest);



if ("BLOCK".equals(risk.getRecommendedAction())) {
    auditLogService.record("LOGIN_BLOCKED_RISK", user.getId(), user.getUsername(),
            "Login blocked by risk engine, score=" + risk.getRiskScore(), httpRequest);
    throw new IllegalArgumentException("This login has been blocked for security reasons. Contact an administrator.");
}

boolean forceTwoFactor = "CHALLENGE".equals(risk.getRecommendedAction()) || user.isTwoFactorEnabled();

if (forceTwoFactor) {
    otpService.issueCode(user.getId(), user.getEmail(), "TWO_FACTOR");
    auditLogService.record("TWO_FACTOR_CODE_SENT", user.getId(), user.getUsername(),
            "2FA code sent for login (risk-driven: " + "CHALLENGE".equals(risk.getRecommendedAction()) + ")", httpRequest);
    return new LoginResponse(new UserResponse(user), false, true);
}

            if (user.isTwoFactorEnabled()) {
                otpService.issueCode(user.getId(), user.getEmail(), "TWO_FACTOR");
                auditLogService.record("TWO_FACTOR_CODE_SENT", user.getId(), user.getUsername(),
                    "2FA code sent for login", httpRequest);
                return new LoginResponse(new UserResponse(user), false, true);
            }

            establishSession(user, authentication, httpRequest, httpResponse);
            auditLogService.record("LOGIN_SUCCESS", user.getId(), user.getUsername(),
                "User logged in", httpRequest);
            return new LoginResponse(new UserResponse(user), false, false);
        } catch (BadCredentialsException ex) {
            recordFailedAttempt(request.getEmail(), httpRequest);
            Long knownUserId = userRepository.findByEmail(request.getEmail())
                .map(User::getId)
                .orElse(null);

            auditLogService.record("LOGIN_FAILED", knownUserId, request.getEmail(),
                "Failed login attempt", httpRequest);
            throw new IllegalArgumentException("Invalid email or password");
        }
    }

    public LoginResponse verifyTwoFactor(TwoFactorVerifyRequest request, HttpServletRequest httpRequest,
                                          HttpServletResponse httpResponse) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid request"));

        boolean valid = otpService.verifyCode(user.getId(), "TWO_FACTOR", request.getCode());
        if (!valid) {
            auditLogService.record("TWO_FACTOR_FAILED", user.getId(), user.getUsername(),
                "Invalid or expired 2FA code", httpRequest);
            throw new IllegalArgumentException("Invalid or expired code");
        }

        var authorities = buildAuthorities(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getEmail(), null, authorities);

        establishSession(user, authentication, httpRequest, httpResponse);
        auditLogService.record("TWO_FACTOR_VERIFIED", user.getId(), user.getUsername(),
            "2FA verified, login complete", httpRequest);

        return new LoginResponse(new UserResponse(user), false, false);
    }

    public LoginResponse verifyDevice(VerifyDeviceRequest request, HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid request"));

        boolean valid = otpService.verifyCode(user.getId(), "DEVICE_VERIFICATION", request.getCode());
        if (!valid) {
            auditLogService.record("DEVICE_VERIFICATION_FAILED", user.getId(), user.getUsername(),
                "Invalid or expired device verification code", httpRequest);
            throw new IllegalArgumentException("Invalid or expired code");
        }

        String deviceId = httpRequest.getHeader("X-Device-Id");
        deviceService.recordSighting(user.getId(), deviceId, httpRequest, request.isRememberDevice());
        auditLogService.record("DEVICE_VERIFIED", user.getId(), user.getUsername(),
            "Device verified" + (request.isRememberDevice() ? " and remembered" : ""), httpRequest);

        

        var authorities = buildAuthorities(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
        establishSession(user, authentication, httpRequest, httpResponse);
        auditLogService.record("LOGIN_SUCCESS", user.getId(), user.getUsername(),
            "User logged in", httpRequest);

        return new LoginResponse(new UserResponse(user), false, false);
    }

    public void resendDeviceCode(ResendCodeRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user ->
            otpService.issueCode(user.getId(), user.getEmail(), "DEVICE_VERIFICATION"));
    }

    public void resendTwoFactorCode(ResendCodeRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user ->
            otpService.issueCode(user.getId(), user.getEmail(), "TWO_FACTOR"));
    }

    private void establishSession(User user, Authentication authentication, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    HttpSession existingSession = httpRequest.getSession(false);
    if (existingSession != null) {
        existingSession.invalidate();
    }

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    HttpSession newSession = httpRequest.getSession(true);
    newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

    sessionRegistry.registerNewSession(newSession.getId(), authentication.getName());

    String deviceId = httpRequest.getHeader("X-Device-Id");
    if (deviceId != null && !deviceId.isBlank()) {
        com.ztp.session.device.ActiveSessionDevice mapping = new com.ztp.session.device.ActiveSessionDevice();
        mapping.setSessionId(newSession.getId());
        mapping.setUserId(user.getId());
        mapping.setDeviceId(deviceId);
        activeSessionDeviceRepository.save(mapping);
    }

    CsrfToken newToken = csrfTokenRepository.generateToken(httpRequest);
    csrfTokenRepository.saveToken(newToken, httpRequest, httpResponse);

    auditLogService.record("SESSION_CREATED", user.getId(), user.getUsername(), "New session established", httpRequest);
}

    private void recordFailedAttempt(String email, HttpServletRequest httpRequest) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isAccountLocked()) {
                return;
            }
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                auditLogService.record("ACCOUNT_LOCKED", user.getId(), user.getUsername(),
                    "Account locked after " + MAX_FAILED_ATTEMPTS + " failed attempts", httpRequest);
            }
            userRepository.save(user);
        });
    }

    private java.util.List<org.springframework.security.core.GrantedAuthority> buildAuthorities(User user) {
        return user.getRole().getPermissions().stream()
            .map(p -> (org.springframework.security.core.GrantedAuthority) new org.springframework.security.core.authority.SimpleGrantedAuthority(p.getName()))
            .toList();
    }
	
	private String resolveIpForRisk(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
}
}