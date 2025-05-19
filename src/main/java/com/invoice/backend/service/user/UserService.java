package com.invoice.backend.service.user;

import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.user.dto.UserDTO;
import com.invoice.backend.common.exceptions.EmailAlreadyExistsException;

public interface UserService {
    User registerUser(UserDTO userDTO) throws EmailAlreadyExistsException;
    User getUserById(Long id);
    User getUserByEmail(String email);
}
