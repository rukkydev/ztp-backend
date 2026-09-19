package com.ztp.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OneTimeCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.ztp.mail.EmailTemplateService emailTemplateService;

    public void issueCode(Long userId, String email, String purpose) {

        /*
         * Invalidate previous unused codes for this purpose.
         */
        codeRepository.findAllByUserIdAndPurposeAndConsumedFalse(userId, purpose)
                .forEach(c -> {
                    c.setConsumed(true);
                    codeRepository.save(c);
                });

        /*
         * Generate a new 6-digit verification code.
         */
        String rawCode = generateCode();

        OneTimeCode code = OneTimeCode.builder()
                .userId(userId)
                .codeHash(passwordEncoder.encode(rawCode))
                .purpose(purpose)
                .consumed(false)
                .expiresAt(
                    LocalDateTime.now().plusMinutes(EXPIRY_MINUTES)
                )
                .build();

        codeRepository.save(code);

        /*
         * Send the code by email.
         *
         * If email delivery fails, immediately invalidate the OTP
         * so that a code the user never received cannot remain usable.
         */
        try {

            emailTemplateService.sendVerificationCodeEmail(
                    email,
                    rawCode,
                    purpose,
                    EXPIRY_MINUTES
            );

        } catch (RuntimeException ex) {

            code.setConsumed(true);
            codeRepository.save(code);

            throw ex;
        }
    }

    public boolean verifyCode(
            Long userId,
            String purpose,
            String submittedCode
    ) {

        var candidates =
                codeRepository.findAllByUserIdAndPurposeAndConsumedFalse(
                        userId,
                        purpose
                );

        for (OneTimeCode code : candidates) {

            if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
                continue;
            }

            if (passwordEncoder.matches(
                    submittedCode,
                    code.getCodeHash()
            )) {

                code.setConsumed(true);
                codeRepository.save(code);

                return true;
            }
        }

        return false;
    }

    private String generateCode() {

        /*
         * Always generates a 6-digit code.
         */
        int code = 100000 + RANDOM.nextInt(900000);

        return String.valueOf(code);
    }
}
