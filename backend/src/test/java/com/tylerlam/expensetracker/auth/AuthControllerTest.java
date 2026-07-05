package com.tylerlam.expensetracker.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tylerlam.expensetracker.auth.dto.AuthResponse;
import com.tylerlam.expensetracker.auth.dto.RegisterRequest;
import com.tylerlam.expensetracker.auth.dto.UserResponse;
import com.tylerlam.expensetracker.security.JwtService;
import com.tylerlam.expensetracker.security.SecurityConfig;
import com.tylerlam.expensetracker.shared.exception.UserAlreadyExistsException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // Satisfies JwtAuthenticationFilter's constructor dependencies, which are pulled into every
    // WebMvcTest slice because it is registered as a servlet Filter.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RegisterRequest buildRequest(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private AuthResponse buildResponse(Long id, String email, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .user(UserResponse.builder().id(id).email(email).build())
                .build();
    }

    // --- POST /api/auth/register ---

    @Test
    public void register_returns201WithAuthResponse() throws Exception {
        RegisterRequest request = buildRequest("user@example.com", "plaintextPassword");
        AuthResponse response = buildResponse(1L, "user@example.com", "jwt-token");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("user@example.com"));
    }

    @Test
    public void register_returns409WhenEmailAlreadyInUse() throws Exception {
        RegisterRequest request = buildRequest("user@example.com", "plaintextPassword");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User with email user@example.com already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User with email user@example.com already exists"));
    }

    @Test
    public void register_returns400WhenEmailIsBlank() throws Exception {
        RegisterRequest request = buildRequest("", "plaintextPassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    public void register_returns400WhenEmailIsInvalid() throws Exception {
        RegisterRequest request = buildRequest("not-an-email", "plaintextPassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    public void register_returns400WhenPasswordTooShort() throws Exception {
        RegisterRequest request = buildRequest("user@example.com", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    public void register_returns400WhenPasswordTooLong() throws Exception {
        RegisterRequest request = buildRequest("user@example.com", "a".repeat(129));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    public void register_returns400WhenBodyIsInvalidJson() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request body"));
    }
}
