package com.example.demo.repositories;

import com.example.demo.TestDataFactory;
import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = TestDataFactory.createTestUser();
    }

    @Test
    void save_ShouldPersistUser() {
        // Given
        User savedUser = TestDataFactory.createTestUser();
        savedUser.setId(1L);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User saved = userRepository.save(testUser);

        // Then
        assertNotNull(saved.getId());
        assertEquals(testUser.getName(), saved.getName());
        assertEquals(testUser.getEmail(), saved.getEmail());
        assertEquals(testUser.getRole(), saved.getRole());
        assertTrue(saved.isEnabled());
        verify(userRepository).save(testUser);
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        // Given
        User savedUser = TestDataFactory.createTestUser();
        savedUser.setId(1L);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        // When
        User saved = userRepository.save(testUser);
        Optional<User> found = userRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getEmail(), found.get().getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> found = userRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        // Given
        User savedUser = TestDataFactory.createTestUser();
        savedUser.setId(1L);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));

        // When
        User saved = userRepository.save(testUser);
        Optional<User> found = userRepository.findByEmail(saved.getEmail());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getEmail(), found.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenNotExists() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void findByEmail_ShouldBeCaseInsensitive() {
        // Given
        User savedUser = TestDataFactory.createTestUser();
        savedUser.setId(1L);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findByEmail("TEST@EXAMPLE.COM")).thenReturn(Optional.of(savedUser));

        // When
        User saved = userRepository.save(testUser);
        Optional<User> found = userRepository.findByEmail(saved.getEmail().toUpperCase());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        verify(userRepository).findByEmail("TEST@EXAMPLE.COM");
    }

    @Test
    void countByRole_ShouldReturnCountOfUsersWithRole() {
        // Given
        when(userRepository.countByRole(Role.CLIENT)).thenReturn(1L);

        // When
        long patientCount = userRepository.countByRole(Role.CLIENT);

        // Then
        assertEquals(1, patientCount);
        verify(userRepository).countByRole(Role.CLIENT);
    }

    @Test
    void findByRole_ShouldReturnUsersWithRole() {
        // Given
        User savedUser = TestDataFactory.createTestUser();
        savedUser.setId(1L);
        
        when(userRepository.findByRole(Role.CLIENT)).thenReturn(Optional.of(savedUser));

        // When
        java.util.Optional<User> patientOpt = userRepository.findByRole(Role.CLIENT);

        // Then
        assertTrue(patientOpt.isPresent());
        assertEquals(Role.CLIENT, patientOpt.get().getRole());
        verify(userRepository).findByRole(Role.CLIENT);
    }

    @Test
    void delete_ShouldRemoveUser() {
        // Given
        User savedUser = TestDataFactory.createTestUser();
        savedUser.setId(1L);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(userRepository).delete(any(User.class));

        // When
        User saved = userRepository.save(testUser);
        Long id = saved.getId();
        userRepository.delete(saved);

        // Then
        verify(userRepository).delete(saved);
        verify(userRepository).save(testUser);
    }
}
