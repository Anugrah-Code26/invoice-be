package com.invoice.backend.infrastructure.user.controller;

import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.user.dto.EmailRequestDTO;
import com.invoice.backend.infrastructure.user.dto.RegistrationDTO;
import com.invoice.backend.infrastructure.user.dto.UserDTO;
import com.invoice.backend.infrastructure.user.repository.UserRepository;
import com.invoice.backend.service.user.EmailService;
import com.invoice.backend.service.user.UserService;
import com.invoice.backend.common.exceptions.EmailAlreadyExistsException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//    @PostMapping("/request-verification")
//    public ResponseEntity<?> requestVerification(@RequestBody EmailRequestDTO req) {
//        try {
//            String message = userService.requestVerification(req);
//            return ResponseEntity.ok(message);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (MessagingException | UnsupportedEncodingException e) {
//            return ResponseEntity.status(500).body("Failed to send verification email");
//        }
//    }
//
//    @PostMapping("/complete-registration")
//    public ResponseEntity<?> completeRegistration(
//            @RequestParam String token,
//            @RequestBody RegistrationDTO dto) throws BadRequestException {
//
//        User user = userRepository.findByVerificationToken(token)
//                .orElseThrow(() -> new BadRequestException("Invalid token"));
//
//        if (user.getTokenExpiryDate().isBefore(Instant.now())) {
//            return ResponseEntity.badRequest().body("Token expired");
//        }
//
//        user.setPassword(passwordEncoder.encode(dto.getPassword()));
//        user.setName(dto.getName());
//        user.setAddress(dto.getAddress());
//        user.setPhoneNumber(dto.getPhoneNumber());
//        user.setEmailVerified(true);
//        user.setVerificationToken(null); // Clear token after use
//        userRepository.save(user);
//
//        return ResponseEntity.ok("Registration completed");
//    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userDTO));
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
