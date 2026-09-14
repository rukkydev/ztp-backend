package com.ztp.account.recovery;

import com.ztp.audit.AuditLogService;
import com.ztp.user.User;
import com.ztp.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RecoveryPhraseService {

    private static final int PHRASE_WORD_COUNT = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;
    private final AuditLogService auditLogService;
	
	private final com.ztp.risk.RiskEvaluationClient riskClient;
	private final com.ztp.risk.RiskLogService riskLogService;

    private List<String> wordList;

    public String generate(Long userId, HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<String> words = loadWordList();
        StringBuilder phrase = new StringBuilder();
        for (int i = 0; i < PHRASE_WORD_COUNT; i++) {
            if (i > 0) phrase.append(" ");
            phrase.append(words.get(RANDOM.nextInt(words.size())));
        }

        String rawPhrase = phrase.toString();
		user.setRecoveryPhraseHash(sha256(rawPhrase));
		user.setRecoveryPhraseSetAt(LocalDateTime.now());
		userRepository.save(user);

        auditLogService.record("RECOVERY_PHRASE_SET", userId, user.getUsername(),
                "Recovery phrase generated/regenerated", httpRequest);

        return rawPhrase;
    }

    public boolean hasRecoveryPhrase(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRecoveryPhraseHash() != null)
                .orElse(false);
    }

    public void recoverAccount(String email, String phrase, String newPassword, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recovery phrase or email"));

        if (user.getRecoveryPhraseHash() == null
                || !passwordEncoder.matches(phrase.trim().toLowerCase(), user.getRecoveryPhraseHash())) {
            auditLogService.record("RECOVERY_PHRASE_FAILED", user.getId(), user.getUsername(),
                    "Failed account recovery attempt via recovery phrase", httpRequest);
            throw new IllegalArgumentException("Invalid recovery phrase or email");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        userRepository.save(user);

        sessionRegistry.getAllSessions(user.getEmail(), false)
                .forEach(SessionInformation::expireNow);
				
				String correlationId = java.util.UUID.randomUUID().toString();
com.ztp.risk.RiskRequest riskRequest = new com.ztp.risk.RiskRequest(
        user.getId(), "RECOVERY_PHRASE_USED", false, false, false, 0, 0,
        null, null, null, resolveIpForRisk(httpRequest)
);
var riskResult = riskClient.evaluateWithMeta(riskRequest);
riskLogService.record(correlationId, riskRequest, riskResult.getResponse(),
        riskResult.getResponse().getRecommendedAction(), riskResult.isEvaluateReached());

if ("BLOCK".equals(riskResult.getResponse().getRecommendedAction())) {
    throw new IllegalArgumentException("Account recovery has been blocked for security reasons.");
}

        auditLogService.record("RECOVERY_PHRASE_USED", user.getId(), user.getUsername(),
                "Account recovered via recovery phrase; password reset, sessions terminated", httpRequest);
    }

    private List<String> loadWordList() {
        if (wordList != null) return wordList;

        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("wordlist.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) words.add(line.trim());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load recovery phrase word list", e);
        }

        wordList = words;
        return words;
    }
	
	private String resolveIpForRisk(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
	}
	
	private String sha256(String input) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException("SHA-256 not available", e);
    }
}
}