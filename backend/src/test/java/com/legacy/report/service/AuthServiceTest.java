package com.legacy.report.service;

import com.legacy.report.dto.LoginRequest;
import com.legacy.report.dto.LoginResponse;
import com.legacy.report.model.User;
import com.legacy.report.repository.UserRepository;
import com.legacy.report.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole("MAKER");
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        String expectedToken = "jwt-token";
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn(expectedToken);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("MAKER", response.getRole());
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
        verify(jwtTokenProvider, times(1)).generateToken(testUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password");
        
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        assertEquals("User not found", exception.getMessage());
        
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIncorrect() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        assertEquals("Invalid password", exception.getMessage());
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void shouldHandleNullUsername() {
        // Given
        LoginRequest loginRequest = new LoginRequest(null, "password");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        assertEquals("Username cannot be null", exception.getMessage());
        
        verify(userRepository, never()).findByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void shouldHandleEmptyUsername() {
        // Given
        LoginRequest loginRequest = new LoginRequest("", "password");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        assertEquals("Username cannot be null", exception.getMessage());
        
        verify(userRepository, never()).findByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void shouldHandleNullPassword() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        assertEquals("Password cannot be null", exception.getMessage());
        
        verify(userRepository, never()).findByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void shouldHandleEmptyPassword() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        assertEquals("Password cannot be null", exception.getMessage());
        
        verify(userRepository, never()).findByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void shouldHandleJwtTokenGenerationError() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenThrow(new RuntimeException("JWT generation failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        assertEquals("JWT generation failed", exception.getMessage());
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
        verify(jwtTokenProvider, times(1)).generateToken(testUser);
    }

    @Test
    void shouldLoginCheckerUser() {
        // Given
        User checkerUser = new User();
        checkerUser.setId(2L);
        checkerUser.setUsername("checker");
        checkerUser.setPassword("encodedPassword");
        checkerUser.setRole("CHECKER");
        
        LoginRequest loginRequest = new LoginRequest("checker", "password");
        String expectedToken = "checker-jwt-token";
        
        when(userRepository.findByUsername("checker")).thenReturn(Optional.of(checkerUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn(expectedToken);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals("checker", response.getUsername());
        assertEquals("CHECKER", response.getRole());
        
        verify(userRepository, times(1)).findByUsername("checker");
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
        verify(jwtTokenProvider, times(1)).generateToken(checkerUser);
    }

    @Test
    void shouldHandleRepositoryException() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        
        when(userRepository.findByUsername("testuser")).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        assertEquals("Database error", exception.getMessage());
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void shouldHandlePasswordEncoderException() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenThrow(new RuntimeException("Password encoding error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        assertEquals("Password encoding error", exception.getMessage());
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }
}
