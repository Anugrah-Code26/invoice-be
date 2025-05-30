package com.invoice.backend.service.auth;

public interface TokenBlacklistService {
    void blacklistToken(String token, String expiredAt);
    boolean isTokenBlacklisted(String token);
}
