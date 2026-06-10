package com.example.demo.services;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RendezVousRepository rendezVousRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.CLIENT);
        testUser.setEnabled(true);
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<User> result = userAdminService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getEmail(), result.getContent().get(0).getEmail());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void disableUser_ShouldDisableUser_WhenUserExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userAdminService.disableUser(1L);

        // Then
        assertNotNull(result);
        assertFalse(result.isEnabled());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void disableUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userAdminService.disableUser(999L));
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void enableUser_ShouldEnableUser_WhenUserExists() {
        // Given
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userAdminService.enableUser(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEnabled());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void enableUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userAdminService.enableUser(999L));
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUserAndReservations_WhenUserExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(rendezVousRepository.findByUser(testUser)).thenReturn(List.of());
        doNothing().when(rendezVousRepository).deleteAll(anyList());
        doNothing().when(userRepository).delete(any(User.class));

        // When
        userAdminService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(rendezVousRepository).findByUser(testUser);
        verify(rendezVousRepository).deleteAll(anyList());
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userAdminService.deleteUser(999L));
        verify(userRepository).findById(999L);
        verify(rendezVousRepository, never()).findByUser(any(User.class));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getUserWithStats_ShouldReturnUserStats() {
        // Given
        when(rendezVousRepository.countByUser(testUser)).thenReturn(5L);

        // When
        Map<String, Object> result = userAdminService.getUserWithStats(testUser);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.get("id"));
        assertEquals(testUser.getName(), result.get("name"));
        assertEquals(testUser.getEmail(), result.get("email"));
        assertEquals(testUser.getRole(), result.get("role"));
        assertEquals(testUser.isEnabled(), result.get("enabled"));
        assertEquals(5L, result.get("reservationCount"));
        verify(rendezVousRepository).countByUser(testUser);
    }

    @Test
    void getAllUsers_ShouldReturnEmptyPage_WhenNoUsersExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of());

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<User> result = userAdminService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void disableUser_ShouldNotChangeOtherUserFields() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userAdminService.disableUser(1L);

        // Then
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getRole(), result.getRole());
        assertFalse(result.isEnabled());
    }

    @Test
    void enableUser_ShouldNotChangeOtherUserFields() {
        // Given
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userAdminService.enableUser(1L);

        // Then
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getRole(), result.getRole());
        assertTrue(result.isEnabled());
    }

    @Test
    void getUserWithStats_ShouldReturnZeroReservationCount_WhenNoReservations() {
        // Given
        when(rendezVousRepository.countByUser(testUser)).thenReturn(0L);

        // When
        Map<String, Object> result = userAdminService.getUserWithStats(testUser);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.get("reservationCount"));
        verify(rendezVousRepository).countByUser(testUser);
    }
}
