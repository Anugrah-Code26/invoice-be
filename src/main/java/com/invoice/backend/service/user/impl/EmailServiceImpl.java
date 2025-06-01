package com.invoice.backend.service.user.impl;

import com.invoice.backend.service.user.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendHtml(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }

    public String loadEmailTemplate(String path, String link) {
        try {
            InputStream inputStream = new ClassPathResource("templates/" + path).getInputStream();
            String html = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return html.replace("{{VERIFICATION_LINK}}", link);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }

    public void sendVerificationEmail(String to, String verificationLink) throws MessagingException {
        String subject = "Complete Your Registration";
        String content = loadEmailTemplate("verification-email.html", verificationLink);
        sendHtml(to, subject, content);
    }

    public void sendInvoiceEmail(String to, String subject, String body, byte[] pdfAttachment) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        if (pdfAttachment != null) {
            helper.addAttachment("invoice.pdf", new ByteArrayResource(pdfAttachment));
        }

        mailSender.send(message);
    }
}
