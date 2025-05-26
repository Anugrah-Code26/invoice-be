package com.invoice.backend.service.user.impl;

import com.invoice.backend.entity.user.Role;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.user.dto.EmailRequestDTO;
import com.invoice.backend.infrastructure.user.dto.UserDTO;
import com.invoice.backend.infrastructure.user.repository.RoleRepository;
import com.invoice.backend.infrastructure.user.repository.UserRepository;
import com.invoice.backend.service.user.EmailService;
import com.invoice.backend.service.user.UserService;
import com.invoice.backend.common.exceptions.EmailAlreadyExistsException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public String requestVerification(EmailRequestDTO req) throws MessagingException, UnsupportedEncodingException {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.generateVerificationToken(); // Assumes this sets the token and expiry
        userRepository.save(user);

        emailService.sendVerificationEmail(req.getEmail(), user.getVerificationToken());

        return "Verification email sent";
    }

    @Override
    public User registerUser(UserDTO userDTO) throws EmailAlreadyExistsException {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setName(userDTO.getName());
        user.setAddress(userDTO.getAddress());
        user.setPhoneNumber(userDTO.getPhoneNumber());

        Optional<Role> defaultRole = roleRepository.findByName("ADMIN");
        if (defaultRole.isPresent()) {
            user.getRoles().add(defaultRole.get());
        } else {
            throw new RuntimeException("Default role not found");
        }

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}