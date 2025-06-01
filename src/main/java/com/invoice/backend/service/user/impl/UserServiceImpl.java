package com.invoice.backend.service.user.impl;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import com.invoice.backend.entity.user.Role;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.user.dto.EmailRequestDTO;
import com.invoice.backend.infrastructure.user.dto.UserDTO;
import com.invoice.backend.infrastructure.user.dto.UserProfileDTO;
import com.invoice.backend.infrastructure.user.repository.RoleRepository;
import com.invoice.backend.infrastructure.user.repository.UserRepository;
import com.invoice.backend.service.user.EmailService;
import com.invoice.backend.service.user.UserService;
import com.invoice.backend.common.exceptions.EmailAlreadyExistsException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public User requestRegistration(EmailRequestDTO req) throws MessagingException {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String token = UUID.randomUUID().toString();
        User user = new User();
        user.setEmail(req.getEmail());
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        String link = frontendUrl + "/verify?verificationToken=" + token;
        emailService.sendVerificationEmail(req.getEmail(), link);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User completeRegistration(String token, UserDTO req) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token expired!");
        }

        user.setName(req.getName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhoneNumber(req.getPhoneNumber());
        user.setAddress(req.getAddress());
        user.setEmailVerified(true);

        Optional<Role> defaultRole = roleRepository.findByName("ADMIN");
        if (defaultRole.isPresent()) {
            user.getRoles().add(defaultRole.get());
        } else {
            throw new RuntimeException("Default role not found");
        }

        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);

        return userRepository.save(user);
    }

//    @Override
//    public User registerUser(UserDTO userDTO) throws EmailAlreadyExistsException {
//        if (userRepository.existsByEmail(userDTO.getEmail())) {
//            throw new EmailAlreadyExistsException("Email already in use");
//        }
//
//        User user = new User();
//        user.setEmail(userDTO.getEmail());
//        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
//        user.setName(userDTO.getName());
//        user.setAddress(userDTO.getAddress());
//        user.setPhoneNumber(userDTO.getPhoneNumber());
//
//        Optional<Role> defaultRole = roleRepository.findByName("ADMIN");
//        if (defaultRole.isPresent()) {
//            user.getRoles().add(defaultRole.get());
//        } else {
//            throw new RuntimeException("Default role not found");
//        }
//
//        return userRepository.save(user);
//    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    @Transactional
    public UserProfileDTO updateUserProfile(Long userId, UserProfileDTO userProfileDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        user.setName(userProfileDTO.getName());
        user.setEmail(userProfileDTO.getEmail());
        user.setPhoneNumber(userProfileDTO.getPhoneNumber());
        user.setAddress(userProfileDTO.getAddress());

        return UserProfileDTO.fromEntity(userRepository.save(user));
    }

}