package com.invoice.backend.service.auth;

import com.invoice.backend.infrastructure.auth.dto.LoginRequestDTO;
import com.invoice.backend.infrastructure.auth.dto.TokenPairResponseDTO;

public interface LoginService {
    TokenPairResponseDTO authenticateUser(LoginRequestDTO req);
}
