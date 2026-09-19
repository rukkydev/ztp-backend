package com.ztp.otp;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OneTimeCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder; // reused purely for hashing, not login
    private final com.ztp.mail.EmailTemplateService emailTemplateService;

    public void issueCode(Long userId, String email, String purpose) {
        // Invalidate any prior unconsumed codes for this purpose first,
        // so only the most recently issued code is ever valid.
        codeRepository.findAllByUserIdAndPurposeAndConsumedFalse(userId, purpose)
                .forEach(c -> { c.setConsumed(true); codeRepository.save(c); });

        String rawCode = generateCode();

        OneTimeCode code = OneTimeCode.builder()
                .userId(userId)
                .codeHash(passwordEncoder.encode(rawCode))
                .purpose(purpose)
                .consumed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .build();
        codeRepository.save(code);

        log.info("==========================================================");
        log.info("OTP CODE FOR {}: [{}] (Purpose: {})", email, rawCode, purpose);
        log.info("==========================================================");

        sendEmail(email, rawCode, purpose);
    }

    public boolean verifyCode(Long userId, String purpose, String submittedCode) {
        var candidates = codeRepository.findAllByUserIdAndPurposeAndConsumedFalse(userId, purpose);

        for (OneTimeCode code : candidates) {
            if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
                continue; // expired, skip
            }
            if (passwordEncoder.matches(submittedCode, code.getCodeHash())) {
                code.setConsumed(true);
                codeRepository.save(code);
                return true;
            }
        }
        return false;
    }

    private String generateCode() {
        int code = 100000 + RANDOM.nextInt(900000); // always 6 digits, no leading zero issue
        return String.valueOf(code);
    }

    private void sendEmail(String to, String rawCode, String purpose) {
        emailTemplateService.sendVerificationCodeEmail(to, rawCode, purpose, EXPIRY_MINUTES);
    }


}