package com.example.demo.services;

import com.example.demo.dto.AuthResponse;
import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.IJwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IJwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.CLIENT);
        testUser.setEnabled(true);
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String token = "jwt-token";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(email, Role.CLIENT.name())).thenReturn(token);

        // When
        AuthResponse response = userService.login(email, password);

        // Then
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(Role.CLIENT.name(), response.getRole());
        assertEquals(email, response.getEmail());
        assertEquals(testUser.getName(), response.getName());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
        verify(jwtService).generateToken(email, Role.CLIENT.name());
    }

    @Test
    void login_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userService.login(email, password));
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_ShouldThrowBadRequestException_WhenPasswordIncorrect() {
        // Given
        String email = "test@example.com";
        String password = "wrongPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.login(email, password));
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void register_ShouldReturnUserWithEncodedPassword() {
        // Given
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";
        testUser.setPassword(rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.register(testUser);

        // Then
        assertNotNull(result);
        assertEquals(encodedPassword, result.getPassword());
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    void register_ShouldThrowException_WhenRepositorySaveFails() {
        // Given
        String rawPassword = "rawPassword";
        testUser.setPassword(rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.register(testUser));
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    void register_ShouldEncodePassword() {
        // Given
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";
        testUser.setPassword(rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.register(testUser);

        // Then
        assertEquals(encodedPassword, result.getPassword());
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(testUser);
    }
}
