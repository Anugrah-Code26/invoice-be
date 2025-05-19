package com.invoice.backend.service.auth.impl;

import com.invoice.backend.infrastructure.repository.RedisTokenRepository;
import com.invoice.backend.service.auth.TokenBlacklistUsecase;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistUsecase {
    private final String REDIS_BLACKLIST_KEY = "eventure_blacklist_token:";
    private final RedisTokenRepository redisTokenRepository;

    public TokenBlacklistServiceImpl(RedisTokenRepository redisTokenRepository) {
        this.redisTokenRepository = redisTokenRepository;
    }

    @Override
    public void blacklistToken(String token, String expiredAt) {
        Duration duration = Duration.between(java.time.Instant.now(), java.time.Instant.parse(expiredAt));
        redisTokenRepository.saveToken(REDIS_BLACKLIST_KEY + token, duration);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return redisTokenRepository.isTokenBlacklisted(REDIS_BLACKLIST_KEY + token);
    }
}
