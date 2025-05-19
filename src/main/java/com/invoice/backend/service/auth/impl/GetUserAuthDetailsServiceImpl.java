package com.invoice.backend.service.auth.impl;

import com.invoice.backend.common.exceptions.DataNotFoundException;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.auth.dto.UserAuth;
import com.invoice.backend.infrastructure.user.repository.UserRepository;
import com.invoice.backend.service.auth.GetUserAuthDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetUserAuthDetailsServiceImpl implements GetUserAuthDetailsService {
    private final UserRepository usersRepository;

    public GetUserAuthDetailsServiceImpl(UserRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User existingUser = usersRepository.findByEmail(username).orElseThrow(() -> new DataNotFoundException("User not found with email: " + username));

        UserAuth userAuth = new UserAuth();
        userAuth.setUser(existingUser);
        return userAuth;
    }
}
