package com.ztp.mail;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);

    private final JavaMailSender mailSender;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from-email:}")
    private String resendFromEmail;

    public void sendVerificationCodeEmail(String toEmail, String code, String purpose, int expiryMinutes) {
        String subject = "Your ZTP Verification Code: " + code;
        String previewText = "Use verification code " + code + " to complete your security challenge.";

        String contentHtml = """
            <p style="margin: 0 0 16px; font-size: 15px; line-height: 24px; color: #334155;">
                A verification request was initiated for your Zero Trust Platform account.
            </p>
            <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin: 24px 0; width: 100%%;">
                <tr>
                    <td align="center">
                        <div style="display: inline-block; background-color: #0f172a; border: 1px solid #1e293b; border-radius: 8px; padding: 18px 36px; letter-spacing: 8px; font-size: 32px; font-weight: 700; color: #38bdf8; font-family: 'Courier New', Courier, monospace;">
                            %s
                        </div>
                    </td>
                </tr>
            </table>
            <p style="margin: 0 0 12px; font-size: 14px; line-height: 22px; color: #475569;">
                <strong>Purpose:</strong> <span style="color: #0f172a;">%s</span><br>
                <strong>Valid for:</strong> %d minutes
            </p>
            <div style="background-color: #fef2f2; border-left: 4px solid #ef4444; border-radius: 4px; padding: 12px 16px; margin: 20px 0 0;">
                <p style="margin: 0; font-size: 13px; line-height: 20px; color: #991b1b;">
                    <strong>Security Alert:</strong> If you did not request this verification code, someone may be attempting to access your account. Please notify your Security Administrator immediately.
                </p>
            </div>
            """.formatted(escapeHtml(code), escapeHtml(purpose), expiryMinutes);

        String fullHtml = wrapInLayout(subject, previewText, contentHtml);
        sendHtmlMail(toEmail, subject, fullHtml);
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink, int expiryMinutes) {
        String subject = "Reset Your ZTP Password";
        String previewText = "Password reset link requested for your Zero Trust Platform account.";

        String contentHtml = """
            <p style="margin: 0 0 16px; font-size: 15px; line-height: 24px; color: #334155;">
                We received a request to reset the password for your Zero Trust Platform account. Click the button below to set a new password:
            </p>
            <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin: 28px 0; width: 100%%;">
                <tr>
                    <td align="center">
                        <a href="%s" target="_blank" style="display: inline-block; background-color: #0284c7; color: #ffffff; text-decoration: none; font-size: 15px; font-weight: 600; padding: 14px 32px; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                            Reset Password
                        </a>
                    </td>
                </tr>
            </table>
            <p style="margin: 0 0 12px; font-size: 13px; line-height: 20px; color: #64748b;">
                This link will expire in <strong>%d minutes</strong>. If the button above does not work, copy and paste this link into your browser:
            </p>
            <p style="margin: 0 0 20px; font-size: 12px; line-height: 18px; color: #0284c7; word-break: break-all;">
                <a href="%s" style="color: #0284c7; text-decoration: underline;">%s</a>
            </p>
            <div style="background-color: #f8fafc; border-left: 4px solid #94a3b8; border-radius: 4px; padding: 12px 16px;">
                <p style="margin: 0; font-size: 13px; line-height: 20px; color: #475569;">
                    If you did not make this request, you can safely disregard this email. Your password will remain unchanged.
                </p>
            </div>
            """.formatted(
                escapeHtml(resetLink),
                expiryMinutes,
                escapeHtml(resetLink),
                escapeHtml(resetLink)
            );

        String fullHtml = wrapInLayout(subject, previewText, contentHtml);
        sendHtmlMail(toEmail, subject, fullHtml);
    }

    public void sendTwoFactorDisabledEmail(String toEmail, String username) {
        String subject = "Notice: Two-Factor Authentication Was Disabled";
        String previewText = "Two-factor authentication has been turned off for your ZTP account.";

        String contentHtml = """
            <p style="margin: 0 0 16px; font-size: 15px; line-height: 24px; color: #334155;">
                Hello <strong>%s</strong>,
            </p>
            <div style="background-color: #fef2f2; border-left: 4px solid #dc2626; border-radius: 4px; padding: 16px; margin: 20px 0;">
                <p style="margin: 0 0 8px; font-size: 14px; font-weight: 700; color: #991b1b;">
                    Two-Factor Authentication Deactivated
                </p>
                <p style="margin: 0; font-size: 13px; line-height: 20px; color: #7f1d1d;">
                    Two-Factor Authentication (2FA) was recently turned off on your ZTP account. Your account security posture has been degraded.
                </p>
            </div>
            <p style="margin: 0 0 16px; font-size: 14px; line-height: 22px; color: #475569;">
                If you initiated this change, no further action is required. If this was <strong>not</strong> you, your account may be compromised. Please log in, re-enable 2FA, and contact your Security Administrator immediately.
            </p>
            """.formatted(escapeHtml(username));

        String fullHtml = wrapInLayout(subject, previewText, contentHtml);
        sendHtmlMail(toEmail, subject, fullHtml);
    }

    private String wrapInLayout(String title, String previewText, String bodyContent) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                <div style="display: none; font-size: 1px; color: #f1f5f9; line-height: 1px; max-height: 0px; max-width: 0px; opacity: 0; overflow: hidden;">
                    %s
                </div>
                <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #f1f5f9; padding: 30px 15px;">
                    <tr>
                        <td align="center">
                            <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 580px; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); border: 1px solid #e2e8f0;">
                                <tr>
                                    <td style="background-color: #0f172a; padding: 24px 32px; border-bottom: 3px solid #0284c7;">
                                        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%%">
                                            <tr>
                                                <td>
                                                    <span style="font-size: 20px; font-weight: 800; letter-spacing: 1.5px; color: #ffffff;">ZTP</span>
                                                    <span style="font-size: 12px; font-weight: 500; color: #94a3b8; margin-left: 8px; text-transform: uppercase; letter-spacing: 1px;">Zero Trust Platform</span>
                                                </td>
                                                <td align="right">
                                                    <span style="display: inline-block; background-color: #1e293b; color: #38bdf8; font-size: 11px; font-weight: 600; padding: 4px 10px; border-radius: 9999px; text-transform: uppercase; letter-spacing: 0.5px;">
                                                        Security Notice
                                                    </span>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 32px;">
                                        %s
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background-color: #f8fafc; padding: 20px 32px; border-top: 1px solid #e2e8f0;">
                                        <p style="margin: 0 0 6px; font-size: 12px; line-height: 18px; color: #64748b;">
                                            This is an automated security message from your organization's <strong>Zero Trust Platform</strong>.
                                        </p>
                                        <p style="margin: 0; font-size: 11px; line-height: 16px; color: #94a3b8;">
                                            Never share verification codes, session keys, or recovery phrases with anyone.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                escapeHtml(title),
                escapeHtml(previewText),
                bodyContent
            );
    }

    private void sendHtmlMail(String to, String subject, String htmlContent) {

        /*
         * Use Resend when RESEND_API_KEY is configured.
         */
        if (resendApiKey != null && !resendApiKey.isBlank()) {

            if (resendFromEmail == null || resendFromEmail.isBlank()) {
                throw new IllegalStateException(
                    "RESEND_FROM_EMAIL is not configured."
                );
            }

            try {
                Resend resend = new Resend(resendApiKey);

                CreateEmailOptions params = CreateEmailOptions.builder()
                        .from(resendFromEmail)
                        .to(to)
                        .subject(subject)
                        .html(htmlContent)
                        .build();

                CreateEmailResponse response = resend.emails().send(params);

                log.info(
                    "Sent security email via Resend to {} with subject: '{}' (Resend ID: {})",
                    to,
                    subject,
                    response.getId()
                );

                return;

            } catch (Exception ex) {

                log.error(
                    "Failed to deliver email via Resend to {}: {}",
                    to,
                    ex.getMessage(),
                    ex
                );

                throw new IllegalStateException(
                    "Failed to deliver email via Resend.",
                    ex
                );
            }
        }

        /*
         * SMTP fallback.
         */
        try {

            if (mailSender == null) {
                throw new IllegalStateException(
                    "JavaMailSender is not configured."
                );
            }

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info(
                "Sent HTML security email via SMTP to {} with subject: '{}'",
                to,
                subject
            );

        } catch (MessagingException | RuntimeException ex) {

            log.error(
                "Failed to deliver HTML email via SMTP to {}: {}",
                to,
                ex.getMessage(),
                ex
            );

            throw new IllegalStateException(
                "Failed to deliver email via SMTP.",
                ex
            );
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }

        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
