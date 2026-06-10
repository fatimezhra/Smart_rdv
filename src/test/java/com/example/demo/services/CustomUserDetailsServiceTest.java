package com.example.demo.services;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.CLIENT);
        testUser.setEnabled(true);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
        assertTrue(userDetails.isEnabled());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("nonexistent@example.com"));
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_ShouldReturnDisabledUser_WhenUserIsDisabled() {
        // Given
        testUser.setEnabled(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_ShouldReturnAdminRole_WhenUserIsAdmin() {
        // Given
        testUser.setRole(Role.ADMIN);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository).findByEmail("admin@example.com");
    }
}
