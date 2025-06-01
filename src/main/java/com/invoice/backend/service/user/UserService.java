package com.invoice.backend.service.user;

import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.user.dto.EmailRequestDTO;
import com.invoice.backend.infrastructure.user.dto.UserDTO;
import com.invoice.backend.common.exceptions.EmailAlreadyExistsException;
import com.invoice.backend.infrastructure.user.dto.UserProfileDTO;
import jakarta.mail.MessagingException;

public interface UserService {
    User requestRegistration(EmailRequestDTO req) throws MessagingException;
    User completeRegistration(String token, UserDTO req);

//    User registerUser(UserDTO userDTO) throws EmailAlreadyExistsException;
    User getUserById(Long id);
    User getUserByEmail(String email);
    UserProfileDTO updateUserProfile(Long userId, UserProfileDTO userProfileDTO);
}
