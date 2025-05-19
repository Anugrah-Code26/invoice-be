package com.invoice.backend.service.auth;

import com.invoice.backend.infrastructure.auth.dto.LogoutRequestDTO;

public interface LogoutService {
    Boolean logoutUser(LogoutRequestDTO req);
}
