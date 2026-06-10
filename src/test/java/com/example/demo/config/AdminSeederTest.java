package com.example.demo.config;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminSeeder adminSeeder;

    @BeforeEach
    void setUp() {
        adminSeeder = new AdminSeeder(userRepository, passwordEncoder);
    }

    @Test
    void seed_ShouldCreateAdmin_WhenNoAdminExists() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        // When
        adminSeeder.seed();

        // Then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void seed_ShouldNotCreateAdmin_WhenAdminExists() {
        // Given
        User existingAdmin = new User();
        existingAdmin.setRole(Role.ADMIN);
        when(userRepository.findAll()).thenReturn(List.of(existingAdmin));

        // When
        adminSeeder.seed();

        // Then
        verify(userRepository, never()).save(any(User.class));
    }
}
