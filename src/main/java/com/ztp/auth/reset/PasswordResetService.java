package com.ztp.auth.reset;

import com.ztp.audit.AuditLogService;
import com.ztp.auth.dto.ForgotPasswordRequest;
import com.ztp.auth.dto.ResetPasswordRequest;
import com.ztp.user.User;
import com.ztp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32; // 256 bits of entropy
    private static final int EXPIRY_MINUTES = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SessionRegistry sessionRegistry;
    private final AuditLogService auditLogService;
	
	private final com.ztp.risk.RiskEvaluationClient riskClient;
	private final com.ztp.risk.RiskLogService riskLogService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void requestReset(ForgotPasswordRequest request) {
        // Always the same response regardless of outcome -- enumeration protection.
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String rawToken = generateToken();
            String hash = sha256(rawToken);

            PasswordResetToken token = PasswordResetToken.builder()
                    .userId(user.getId())
                    .tokenHash(hash)
                    .consumed(false)
                    .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                    .build();
            tokenRepository.save(token);

            sendResetEmail(user.getEmail(), rawToken);
            auditLogService.record("PASSWORD_RESET_REQUESTED", user.getId(), user.getUsername(),
                    "Password reset link requested", null);
        });
    }

    public void resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest) {
        String hash = sha256(request.getToken());

        PasswordResetToken token = tokenRepository.findByTokenHashAndConsumedFalse(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired reset link");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        userRepository.save(user);

        token.setConsumed(true);
        tokenRepository.save(token);

        // A password reset is a strong signal the account may have been
        // compromised -- terminate every existing session as a precaution.
        sessionRegistry.getAllSessions(user.getEmail(), false)
                .forEach(SessionInformation::expireNow);
				
		String correlationId = java.util.UUID.randomUUID().toString();
		com.ztp.risk.RiskRequest riskRequest = new com.ztp.risk.RiskRequest(
				user.getId(), "PASSWORD_RESET_COMPLETED", false, false, false, 0, 0,
				null, null, null, resolveIpForRisk(httpRequest)
		);
		var riskResult = riskClient.evaluateWithMeta(riskRequest);
		riskLogService.record(correlationId, riskRequest, riskResult.getResponse(),
				riskResult.getResponse().getRecommendedAction(), riskResult.isEvaluateReached());

		if ("BLOCK".equals(riskResult.getResponse().getRecommendedAction())) {
			throw new IllegalArgumentException("This action has been blocked for security reasons.");
		}

        auditLogService.record("PASSWORD_RESET_COMPLETED", user.getId(), user.getUsername(),
                "Password was reset; all sessions terminated", null);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void sendResetEmail(String to, String rawToken) {
    String resetLink = frontendUrl + "/auth/reset-password.html?token=" + rawToken;
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset your ZTP password");
        message.setText("Click the link below to reset your password:\n" + resetLink
                + "\nThis link expires in " + EXPIRY_MINUTES + " minutes."
                + "\nIf you didn't request this, you can safely ignore this email.");
        mailSender.send(message);
    } catch (Exception ex) {
        // Don't let a mail-provider outage take down the reset flow.
        // The token is already persisted -- log loudly so this doesn't
        // go unnoticed, but let the request complete normally.
        org.slf4j.LoggerFactory.getLogger(PasswordResetService.class)
                .error("Failed to send password reset email to {}: {}", to, ex.getMessage());
    }
}

private String resolveIpForRisk(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
}


}





