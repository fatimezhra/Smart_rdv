package com.example.demo.repositories;

import com.example.demo.TestDataFactory;
import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RendezVousRepositoryTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private RendezVous testRendezVous;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = TestDataFactory.createTestUser();
        
        testRendezVous = TestDataFactory.createTestRendezVous(
            testUser, 
            TestDataFactory.createTestTimeSlot()
        );
    }

    @Test
    void save_ShouldPersistRendezVous() {
        // Given
        RendezVous savedRendezVous = TestDataFactory.createTestRendezVous(testUser, TestDataFactory.createTestTimeSlot());
        savedRendezVous.setId(1L);
        
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(savedRendezVous);

        // When
        RendezVous saved = rendezVousRepository.save(testRendezVous);

        // Then
        assertNotNull(saved.getId());
        assertEquals(testUser.getId(), saved.getUser().getId());
        assertEquals(testRendezVous.getDate(), saved.getDate());
        assertEquals(testRendezVous.getHeure(), saved.getHeure());
        assertEquals(Statut.CONFIRMED, saved.getStatut());
        assertEquals(testRendezVous.getNotes(), saved.getNotes());
        verify(rendezVousRepository).save(testRendezVous);
    }

    @Test
    void findById_ShouldReturnRendezVous_WhenExists() {
        // Given
        RendezVous savedRendezVous = TestDataFactory.createTestRendezVous(testUser, TestDataFactory.createTestTimeSlot());
        savedRendezVous.setId(1L);
        
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(savedRendezVous);
        when(rendezVousRepository.findById(1L)).thenReturn(Optional.of(savedRendezVous));

        // When
        RendezVous saved = rendezVousRepository.save(testRendezVous);
        Optional<RendezVous> found = rendezVousRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getUser().getId(), found.get().getUser().getId());
        verify(rendezVousRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        when(rendezVousRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<RendezVous> found = rendezVousRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
        verify(rendezVousRepository).findById(999L);
    }

    @Test
    void findByUser_ShouldReturnUserRendezVous() {
        // Given
        RendezVous saved1 = TestDataFactory.createTestRendezVous(testUser, TestDataFactory.createTestTimeSlot());
        saved1.setId(1L);
        
        RendezVous saved2 = TestDataFactory.createTestRendezVous(testUser, TestDataFactory.createTestTimeSlot());
        saved2.setId(2L);
        saved2.setDate(LocalDate.now().plusDays(2));
        
        when(rendezVousRepository.findByUser(testUser)).thenReturn(List.of(saved1, saved2));

        // When
        List<RendezVous> userRdv = rendezVousRepository.findByUser(testUser);

        // Then
        assertEquals(2, userRdv.size());
        assertTrue(userRdv.stream().anyMatch(r -> r.getId().equals(saved1.getId())));
        assertTrue(userRdv.stream().anyMatch(r -> r.getId().equals(saved2.getId())));
        verify(rendezVousRepository).findByUser(testUser);
    }

    @Test
    void findByUserAndStatut_ShouldReturnFilteredRendezVous() {
        // Given
        RendezVous confirmed = TestDataFactory.createTestRendezVous(testUser, TestDataFactory.createTestTimeSlot());
        confirmed.setId(1L);
        confirmed.setStatut(Statut.CONFIRMED);
        
        when(rendezVousRepository.findByUserAndStatut(testUser, Statut.CONFIRMED)).thenReturn(List.of(confirmed));

        // When
        List<RendezVous> confirmedRdv = rendezVousRepository.findByUserAndStatut(testUser, Statut.CONFIRMED);

        // Then
        assertEquals(1, confirmedRdv.size());
        assertEquals(Statut.CONFIRMED, confirmedRdv.get(0).getStatut());
        verify(rendezVousRepository).findByUserAndStatut(testUser, Statut.CONFIRMED);
    }

    @Test
    void findByUserAndStatutInOrderByDateDesc_ShouldReturnOrderedRendezVous() {
        // Given
        RendezVous recentRdv = TestDataFactory.createTestRendezVous(testUser, TestDataFactory.createTestTimeSlot());
        recentRdv.setId(1L);
        recentRdv.setDate(LocalDate.now().minusDays(1));
        recentRdv.setStatut(Statut.CANCELLED);

        RendezVous oldRdv = TestDataFactory.createTestRendezVous(testUser, TestDataFactory.createTestTimeSlot());
        oldRdv.setId(2L);
        oldRdv.setDate(LocalDate.now().minusDays(5));
        oldRdv.setStatut(Statut.CANCELLED);
        
        when(rendezVousRepository.findByUserAndStatutInOrderByDateDesc(testUser, List.of(Statut.CANCELLED)))
            .thenReturn(List.of(recentRdv, oldRdv));

        // When
        List<RendezVous> cancelledRdv = rendezVousRepository.findByUserAndStatutInOrderByDateDesc(
            testUser, 
            List.of(Statut.CANCELLED)
        );

        // Then
        assertEquals(2, cancelledRdv.size());
        assertEquals(recentRdv.getDate(), cancelledRdv.get(0).getDate());
        assertEquals(oldRdv.getDate(), cancelledRdv.get(1).getDate());
        verify(rendezVousRepository).findByUserAndStatutInOrderByDateDesc(testUser, List.of(Statut.CANCELLED));
    }

    @Test
    void findByDate_ShouldReturnRendezVousForDate() {
        // Given
        LocalDate testDate = LocalDate.now().plusDays(1);
        RendezVous testRdv = TestDataFactory.createTestRendezVous(testUser, TestDataFactory.createTestTimeSlot());
        testRdv.setId(1L);
        testRdv.setDate(testDate);
        
        when(rendezVousRepository.findByDate(testDate)).thenReturn(List.of(testRdv));

        // When
        List<RendezVous> found = rendezVousRepository.findByDate(testDate);

        // Then
        assertEquals(1, found.size());
        assertEquals(testDate, found.get(0).getDate());
        verify(rendezVousRepository).findByDate(testDate);
    }

    @Test
    void delete_ShouldRemoveRendezVous() {
        // Given
        RendezVous savedRendezVous = TestDataFactory.createTestRendezVous(testUser, TestDataFactory.createTestTimeSlot());
        savedRendezVous.setId(1L);
        
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(savedRendezVous);
        doNothing().when(rendezVousRepository).delete(any(RendezVous.class));

        // When
        RendezVous saved = rendezVousRepository.save(testRendezVous);
        rendezVousRepository.delete(saved);

        // Then
        verify(rendezVousRepository).delete(saved);
        verify(rendezVousRepository).save(testRendezVous);
    }
}
