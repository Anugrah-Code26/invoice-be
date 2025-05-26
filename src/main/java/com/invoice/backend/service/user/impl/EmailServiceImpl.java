package com.invoice.backend.service.user.impl;

import ch.qos.logback.core.model.processor.DefaultProcessor;
import com.invoice.backend.service.user.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendVerificationEmail(String email, String token) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Set email basics
        helper.setTo(email);
        helper.setSubject("Verify Your Account");
        helper.setFrom("no-reply@MyInvoice.com", "MyInvoice");

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("verificationLink",
                "https://MyInvoice.com/verify-email?token=" + token);

        // Process HTML template
        String htmlContent = templateEngine.process("email-verification", context);

        // Set email content
        helper.setText(htmlContent, true);

        // Add logo as inline attachment
        ClassPathResource logo = new ClassPathResource("static/images/MyInvoice-Logo.png");
        helper.addInline("logo", logo, "image/png");

        mailSender.send(message);
    }
}
