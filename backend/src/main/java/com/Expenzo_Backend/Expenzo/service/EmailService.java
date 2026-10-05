package com.Expenzo_Backend.Expenzo.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ─── Send HTML Email (base method) ────────────────────────
    // NOTE: This method is intentionally NOT @Async so exceptions surface
    // in the caller's thread. All public methods call this.
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        log.info("📧 Sending email to: {} | subject: {}", to, subject);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "Expenzo");
            helper.setReplyTo(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(stripHtml(htmlBody), htmlBody);
            mailSender.send(message);

            log.info("✅ Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    // ─── Welcome Email with Auto-Generated Password ──────────
    @Async
    public void sendWelcomeEmail(String to, String name, String tempPassword) {
        String subject = "Welcome to Expenzo! Your Account is Ready";
        String html = """
            <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08)">
              <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px 24px;text-align:center">
                <h1 style="color:#fff;margin:0;font-size:28px;font-weight:700">Welcome to Expenzo!</h1>
                <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px">Your smart expense manager</p>
              </div>
              <div style="padding:32px 24px">
                <p style="color:#1f2937;font-size:16px;margin:0 0 16px">Hi <strong>%s</strong>,</p>
                <p style="color:#4b5563;font-size:14px;line-height:1.6;margin:0 0 24px">
                  Your Expenzo account has been created successfully! Use the temporary password below to log in for the first time.
                </p>
                <div style="background:#f3f4f6;border-radius:8px;padding:20px;text-align:center;margin:0 0 24px">
                  <p style="color:#6b7280;font-size:12px;text-transform:uppercase;letter-spacing:1px;margin:0 0 8px">Your Temporary Password</p>
                  <p style="color:#1f2937;font-size:24px;font-weight:700;font-family:monospace;margin:0;letter-spacing:2px">%s</p>
                </div>
                <div style="background:#fef3c7;border:1px solid #f59e0b;border-radius:8px;padding:12px 16px;margin:0 0 24px">
                  <p style="color:#92400e;font-size:13px;margin:0">
                    <strong>⚠️ Important:</strong> Please change your password immediately after your first login for security.
                  </p>
                </div>
                <a href="%s/login" style="display:block;background:linear-gradient(135deg,#6366f1,#8b5cf6);color:#fff;text-decoration:none;text-align:center;padding:14px;border-radius:8px;font-weight:600;font-size:15px">
                  Log In to Expenzo →
                </a>
              </div>
              <div style="background:#f9fafb;padding:16px 24px;text-align:center;border-top:1px solid #e5e7eb">
                <p style="color:#9ca3af;font-size:12px;margin:0">Expenzo — Track Every Rupee</p>
              </div>
            </div>
            """.formatted(name, tempPassword, frontendUrl);

        sendHtmlEmail(to, subject, html);
    }

    // ─── OTP Verification Email ───────────────────────────────
    @Async
    public void sendOtpEmail(String to, String name, String otp) {
        String subject = "Expenzo — Verify Your Email";
        String html = """
            <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08)">
              <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px 24px;text-align:center">
                <h1 style="color:#fff;margin:0;font-size:28px;font-weight:700">Verify Your Email</h1>
                <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px">One more step to get started</p>
              </div>
              <div style="padding:32px 24px">
                <p style="color:#1f2937;font-size:16px;margin:0 0 16px">Hi <strong>%s</strong>,</p>
                <p style="color:#4b5563;font-size:14px;line-height:1.6;margin:0 0 24px">
                  Enter this 6-digit code on the verification screen to activate your account:
                </p>
                <div style="background:#f3f4f6;border-radius:8px;padding:24px;text-align:center;margin:0 0 24px">
                  <p style="color:#6b7280;font-size:12px;text-transform:uppercase;letter-spacing:1px;margin:0 0 8px">Verification Code</p>
                  <p style="color:#1f2937;font-size:36px;font-weight:700;font-family:monospace;margin:0;letter-spacing:8px">%s</p>
                </div>
                <div style="background:#fef3c7;border:1px solid #f59e0b;border-radius:8px;padding:12px 16px;margin:0 0 16px">
                  <p style="color:#92400e;font-size:13px;margin:0">
                    <strong>⏰ Expires in 10 minutes.</strong> If you didn't create an Expenzo account, ignore this email.
                  </p>
                </div>
              </div>
              <div style="background:#f9fafb;padding:16px 24px;text-align:center;border-top:1px solid #e5e7eb">
                <p style="color:#9ca3af;font-size:12px;margin:0">Expenzo — Track Every Rupee</p>
              </div>
            </div>
            """.formatted(name, otp);

        sendHtmlEmail(to, subject, html);
    }

    // ─── Password Reset Link Email ────────────────────────────
    @Async
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        String subject = "Expenzo — Reset Your Password";
        String html = """
            <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08)">
              <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px 24px;text-align:center">
                <h1 style="color:#fff;margin:0;font-size:28px;font-weight:700">Reset Password</h1>
                <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px">We got your request</p>
              </div>
              <div style="padding:32px 24px">
                <p style="color:#1f2937;font-size:16px;margin:0 0 16px">Hi <strong>%s</strong>,</p>
                <p style="color:#4b5563;font-size:14px;line-height:1.6;margin:0 0 24px">
                  Click the button below to reset your Expenzo password. This link is valid for <strong>30 minutes</strong>.
                </p>
                <a href="%s" style="display:block;background:linear-gradient(135deg,#6366f1,#8b5cf6);color:#fff;text-decoration:none;text-align:center;padding:14px;border-radius:8px;font-weight:600;font-size:15px;margin:0 0 24px">
                  Reset My Password →
                </a>
                <div style="background:#fef2f2;border:1px solid #fca5a5;border-radius:8px;padding:12px 16px">
                  <p style="color:#991b1b;font-size:13px;margin:0">
                    <strong>🔒 Didn't request this?</strong> Ignore this email — your password won't change.
                  </p>
                </div>
              </div>
              <div style="background:#f9fafb;padding:16px 24px;text-align:center;border-top:1px solid #e5e7eb">
                <p style="color:#9ca3af;font-size:12px;margin:0">Expenzo — Track Every Rupee</p>
              </div>
            </div>
            """.formatted(name, resetLink);

        sendHtmlEmail(to, subject, html);
    }

    // ─── Budget Overspend Alert Email ─────────────────────────
    // Kept public — reserved for future use by the budget module.
    // Marked @SuppressWarnings to silence the "never used" warning.
    @Async
    @SuppressWarnings("unused")
    public void sendBudgetOverspendAlert(String to, String name,
                                         String categoryName,
                                         double spent, double budget,
                                         int percentUsed) {
        String statusColor = percentUsed >= 100 ? "#dc2626" : "#f59e0b";
        String statusText = percentUsed >= 100 ? "EXCEEDED" : "WARNING (" + percentUsed + "%)";
        String subject = percentUsed >= 100
                ? "🚨 Expenzo — Budget Exceeded for " + categoryName
                : "⚠️ Expenzo — Budget Warning for " + categoryName;

        String html = """
            <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08)">
              <div style="background:%s;padding:32px 24px;text-align:center">
                <h1 style="color:#fff;margin:0;font-size:28px;font-weight:700">Budget %s</h1>
                <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px">%s</p>
              </div>
              <div style="padding:32px 24px">
                <p style="color:#1f2937;font-size:16px;margin:0 0 16px">Hi <strong>%s</strong>,</p>
                <p style="color:#4b5563;font-size:14px;line-height:1.6;margin:0 0 24px">
                  Your spending in <strong>%s</strong> has reached <strong>%d%%</strong> of your monthly budget.
                </p>
                <div style="background:#f3f4f6;border-radius:8px;padding:20px;margin:0 0 24px">
                  <table style="width:100%%;border-collapse:collapse">
                    <tr>
                      <td style="color:#6b7280;font-size:13px;padding:6px 0">Category</td>
                      <td style="color:#1f2937;font-size:13px;font-weight:600;text-align:right;padding:6px 0">%s</td>
                    </tr>
                    <tr>
                      <td style="color:#6b7280;font-size:13px;padding:6px 0">Budget Limit</td>
                      <td style="color:#1f2937;font-size:13px;font-weight:600;text-align:right;padding:6px 0">₹%s</td>
                    </tr>
                    <tr>
                      <td style="color:#6b7280;font-size:13px;padding:6px 0">Amount Spent</td>
                      <td style="color:%s;font-size:13px;font-weight:700;text-align:right;padding:6px 0">₹%s</td>
                    </tr>
                    <tr>
                      <td style="color:#6b7280;font-size:13px;padding:6px 0">Status</td>
                      <td style="color:%s;font-size:13px;font-weight:700;text-align:right;padding:6px 0">%d%% used</td>
                    </tr>
                  </table>
                </div>
                <a href="%s/budgets" style="display:block;background:linear-gradient(135deg,#6366f1,#8b5cf6);color:#fff;text-decoration:none;text-align:center;padding:14px;border-radius:8px;font-weight:600;font-size:15px">
                  Review Budgets →
                </a>
              </div>
              <div style="background:#f9fafb;padding:16px 24px;text-align:center;border-top:1px solid #e5e7eb">
                <p style="color:#9ca3af;font-size:12px;margin:0">Expenzo — Track Every Rupee</p>
              </div>
            </div>
            """.formatted(
                statusColor,
                statusText,
                categoryName,
                name,
                categoryName,
                percentUsed,
                categoryName,
                String.format("%,.0f", budget),
                statusColor,
                String.format("%,.0f", spent),
                statusColor,
                percentUsed,
                frontendUrl
        );

        sendHtmlEmail(to, subject, html);
    }

    // ─── Strip HTML for text/plain alternative ────────────────
    private String stripHtml(String html) {
        return html
                .replaceAll("<style[^>]*>.*?</style>", "")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")     // ✅ plain replace, not replaceAll
                .replaceAll("\\s+", " ")
                .trim();
    }
}