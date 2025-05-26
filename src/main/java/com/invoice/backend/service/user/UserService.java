package com.invoice.backend.service.user;

import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.user.dto.EmailRequestDTO;
import com.invoice.backend.infrastructure.user.dto.UserDTO;
import com.invoice.backend.common.exceptions.EmailAlreadyExistsException;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface UserService {
    String requestVerification(EmailRequestDTO req) throws MessagingException, UnsupportedEncodingException;

    User registerUser(UserDTO userDTO) throws EmailAlreadyExistsException;
    User getUserById(Long id);
    User getUserByEmail(String email);
}
