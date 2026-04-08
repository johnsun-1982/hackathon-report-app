package com.legacy.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legacy.report.dto.LoginRequest;
import com.legacy.report.dto.LoginResponse;
import com.legacy.report.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        LoginResponse loginResponse = new LoginResponse("jwt-token", "testuser", "MAKER");
        
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.token").value("jwt-token"))
               .andExpect(jsonPath("$.username").value("testuser"))
               .andExpect(jsonPath("$.role").value("MAKER"));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestForMissingUsername() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(null, "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForMissingPassword() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForEmptyUsername() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("", "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForEmptyPassword() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForMalformedRequest() throws Exception {
        // Given
        String malformedJson = "{\"username\": \"testuser\"}"; // Missing password

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleServiceException() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
               .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldLoginCheckerUser() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("checker", "password");
        LoginResponse loginResponse = new LoginResponse("checker-token", "checker", "CHECKER");
        
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.token").value("checker-token"))
               .andExpect(jsonPath("$.username").value("checker"))
               .andExpect(jsonPath("$.role").value("CHECKER"));
    }

    @Test
    void shouldReturnBadRequestForInvalidMediaType() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content("username=testuser&password=password"))
               .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldHandleNullLoginRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
               .andExpect(status().isBadRequest());
    }
}
