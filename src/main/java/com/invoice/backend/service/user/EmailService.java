package com.invoice.backend.service.user;

import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailService {
    public void sendVerificationEmail(String email, String token) throws MessagingException, UnsupportedEncodingException;
}
