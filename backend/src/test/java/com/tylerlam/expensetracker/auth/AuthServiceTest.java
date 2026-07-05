package com.tylerlam.expensetracker.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tylerlam.expensetracker.auth.dto.AuthResponse;
import com.tylerlam.expensetracker.auth.dto.RegisterRequest;
import com.tylerlam.expensetracker.security.JwtService;
import com.tylerlam.expensetracker.shared.exception.UserAlreadyExistsException;
import com.tylerlam.expensetracker.user.User;
import com.tylerlam.expensetracker.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest buildRequest(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    // --- register ---

    @Test
    public void register_savesUserAndReturnsAuthResponse() {
        RegisterRequest request = buildRequest("user@example.com", "plaintextPassword");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintextPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse result = authService.register(request);

        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
        assertThat(result.getUser().getId()).isEqualTo(1L);
        assertThat(result.getUser().getEmail()).isEqualTo("user@example.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(captor.getValue().getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    public void register_throwsUserAlreadyExistsException_whenEmailInUse() {
        RegisterRequest request = buildRequest("user@example.com", "plaintextPassword");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email user@example.com already exists");

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any(User.class));
    }
}
