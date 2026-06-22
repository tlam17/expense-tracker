package com.tylerlam.expensetracker.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tylerlam.expensetracker.auth.dto.AuthResponse;
import com.tylerlam.expensetracker.auth.dto.RegisterRequest;
import com.tylerlam.expensetracker.auth.dto.UserResponse;
import com.tylerlam.expensetracker.security.JwtService;
import com.tylerlam.expensetracker.shared.exception.UserAlreadyExistsException;
import com.tylerlam.expensetracker.user.User;
import com.tylerlam.expensetracker.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // Register a new user
    public AuthResponse register(RegisterRequest request) {
        // Check if the email is already in use
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create a new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);

        // Generate JWT token for the new user
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .user(toResponse(user))
                .build();
    }

    // Helper method to convert User entity to UserResponse DTO
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }
}
