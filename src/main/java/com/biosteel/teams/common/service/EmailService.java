package com.biosteel.teams.common.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.biosteel.teams.common.config.EmailProperties;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailProperties emailProperties;

    @Async
    public void sendVerificationEmail(String to, String token) {
        if (emailProperties.getVerificationBaseUrl() == null) {
            throw new IllegalStateException("Verification base URL is not configured");
        }
        if (emailProperties.getSupportEmail() == null) {
            throw new IllegalStateException("Support email is not configured");
        }

        Context context = new Context();
        Map<String, Object> variables = Map.of(
                "verificationToken", token,
                "supportEmail", emailProperties.getSupportEmail());
        context.setVariables(variables);

        String htmlContent = templateEngine.process("verification-email", context);

        try {
            sendHtmlEmail(
                    to,
                    "Verify Your BioSteel Account",
                    htmlContent);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}", to, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        Context context = new Context();
        context.setVariables(Map.of(
                "resetToken", token,
                "supportEmail", emailProperties.getSupportEmail()));

        String htmlContent = templateEngine.process("password-reset-email", context);

        try {
            sendHtmlEmail(
                    to,
                    "Reset Your BioSteel Password",
                    htmlContent);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        Context context = new Context();
        context.setVariables(Map.of(
                "firstName", firstName != null ? firstName : "User",
                "supportEmail", emailProperties.getSupportEmail()));

        String htmlContent = templateEngine.process("welcome-email", context);

        try {
            sendHtmlEmail(
                    to,
                    "Welcome to BioSteel!",
                    htmlContent);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}", to, e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    @Async
    public void sendInvitationEmail(String to, String firstName, String teamName, String invitationCode,
            int expirationHours) {
        Context context = new Context();
        context.setVariables(Map.of(
                "firstName", firstName != null ? firstName : "User",
                "teamName", teamName,
                "invitationCode", invitationCode,
                "expirationHours", expirationHours,
                "supportEmail", emailProperties.getSupportEmail()));

        String htmlContent = templateEngine.process("invitation-email", context);

        try {
            sendHtmlEmail(
                    to,
                    "You've been invited to join " + teamName + " on BioSteel Teams",
                    htmlContent);
        } catch (MessagingException e) {
            log.error("Failed to send invitation email to {}", to, e);
            throw new RuntimeException("Failed to send invitation email", e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        helper.setFrom(emailProperties.getFromEmail());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}