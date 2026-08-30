package com.library.application.service.impl;

import com.library.application.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setTo(to);
            mailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", to);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new MailSendException("Failed to send email");
        }
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        String html = loadTemplate("templates/email/reset-password.html")
                .replace("{{resetLink}}", resetLink);
        sendEmail(to, "Reset your password", html);
    }

    private String loadTemplate(String classpathLocation) {
        try {
            var resource = new ClassPathResource(classpathLocation);
            return new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load email template: {}", classpathLocation, e);
            throw new IllegalStateException("Missing email template: " + classpathLocation);
        }
    }
}
