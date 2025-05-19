package com.invoice.backend.service.auth;

import com.invoice.backend.infrastructure.auth.dto.TokenPairResponseDTO;

public interface TokenRefreshService {
    TokenPairResponseDTO refreshAccessToken(String refreshToken);
}
