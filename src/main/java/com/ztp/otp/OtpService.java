package com.ztp.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OneTimeCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder; // reused purely for hashing, not login
    private final JavaMailSender mailSender;

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
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your ZTP verification code");
        message.setText("Your verification code is: " + rawCode
                + "\nThis code expires in " + EXPIRY_MINUTES + " minutes."
                + "\nPurpose: " + purpose);
        mailSender.send(message);
    } catch (Exception ex) {
        // Don't let a mail-provider outage take down the login flow.
        // The code is already persisted -- log loudly so this doesn't
        // go unnoticed, but let the request complete normally.
        org.slf4j.LoggerFactory.getLogger(OtpService.class)
                .error("Failed to send OTP email to {} for purpose {}: {}", to, purpose, ex.getMessage());
    }
}


}