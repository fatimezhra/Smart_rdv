package com.example.demo.services;

import com.example.demo.TestDataFactory;
import com.example.demo.TestUtils;
import com.example.demo.entities.*;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.TimeSlotRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.WaitingListRepository;
import com.example.demo.repositories.BlockedDateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WaitingListRepository waitingListRepository;

    @Mock
    private BlockedDateRepository blockedDateRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User testUser;
    private TimeSlot testTimeSlot;
    private RendezVous testRendezVous;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = TestDataFactory.createTestUser();
        testTimeSlot = TestDataFactory.createTestTimeSlot();
        testRendezVous = TestDataFactory.createTestRendezVous(testUser, testTimeSlot);
    }

    @Test
    void reserver_ShouldCreateConfirmedAppointment_WhenSlotIsAvailable() {
        // Given
        testTimeSlot.setDisponible(true);
        when(timeSlotRepository.findById(anyLong())).thenReturn(Optional.of(testTimeSlot));
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(testRendezVous);

        // When
        Object result = reservationService.reserver(1L, testUser);

        // Then
        assertNotNull(result);
        verify(timeSlotRepository).save(testTimeSlot);
        verify(rendezVousRepository).save(any(RendezVous.class));
        assertFalse(testTimeSlot.isDisponible());
    }

    @Test
    void reserver_ShouldAddToWaitingList_WhenNoSlotsAvailable() {
        // Given
        testTimeSlot.setDisponible(false);
        when(timeSlotRepository.findById(anyLong())).thenReturn(Optional.of(testTimeSlot));
        when(rendezVousRepository.existsByUserAndDateAndStatut(eq(testUser), any(LocalDate.class), eq(Statut.CONFIRMED)))
                .thenReturn(false);

        // When
        Object result = reservationService.reserver(1L, testUser);

        // Then
        assertNotNull(result);
        verify(waitingListRepository).save(any(WaitingList.class));
    }

    @Test
    void annuler_ShouldCancelAppointment_WhenAppointmentExists() {
        // Given
        when(rendezVousRepository.findById(1L)).thenReturn(Optional.of(testRendezVous));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(testTimeSlot);

        // When
        reservationService.annuler(1L);

        // Then
        verify(rendezVousRepository).save(testRendezVous);
        assertEquals(Statut.CANCELLED, testRendezVous.getStatut());
        verify(timeSlotRepository).save(testTimeSlot);
        assertTrue(testTimeSlot.isDisponible());
    }

    @Test
    void annuler_ShouldThrowException_WhenAppointmentNotFound() {
        // Given
        when(rendezVousRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> reservationService.annuler(1L));
    }

    @Test
    void reschedule_ShouldUpdateAppointment_WhenNewSlotIsAvailable() {
        // Given
        TimeSlot newSlot = TestDataFactory.createTestTimeSlot();
        newSlot.setHeure(LocalTime.of(14, 0));
        newSlot.setDisponible(true);

        when(rendezVousRepository.findById(1L)).thenReturn(Optional.of(testRendezVous));
        when(timeSlotRepository.findById(2L)).thenReturn(Optional.of(newSlot));
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(testRendezVous);

        // When
        RendezVous result = reservationService.reschedule(1L, 2L, testUser);

        // Then
        assertNotNull(result);
        verify(rendezVousRepository).save(testRendezVous);
        verify(timeSlotRepository).save(testTimeSlot);
        verify(timeSlotRepository).save(newSlot);
        assertTrue(testTimeSlot.isDisponible());
        assertFalse(newSlot.isDisponible());
    }

    @Test
    void reschedule_ShouldThrowException_WhenAppointmentNotFound() {
        // Given
        when(rendezVousRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                reservationService.reschedule(1L, 2L, testUser));
    }

    @Test
    void reschedule_ShouldThrowException_WhenUserNotAuthorized() {
        // Given
        User differentUser = TestDataFactory.createTestUser();
        differentUser.setEmail("different@example.com");
        testRendezVous.setUser(differentUser);

        when(rendezVousRepository.findById(1L)).thenReturn(Optional.of(testRendezVous));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                reservationService.reschedule(1L, 2L, testUser));
    }

    @Test
    void addNotes_ShouldUpdateAppointmentNotes_WhenUserIsAuthorized() {
        // Given
        String notes = "Updated notes";
        when(rendezVousRepository.findById(1L)).thenReturn(Optional.of(testRendezVous));
        when(rendezVousRepository.save(any(RendezVous.class))).thenReturn(testRendezVous);

        // When
        RendezVous result = reservationService.addNotes(1L, notes, testUser);

        // Then
        assertNotNull(result);
        verify(rendezVousRepository).save(testRendezVous);
        assertEquals(notes, testRendezVous.getNotes());
    }

    @Test
    void addNotes_ShouldThrowException_WhenUserNotAuthorized() {
        // Given
        User differentUser = TestDataFactory.createTestUser();
        differentUser.setId(999L); // Different ID to trigger authorization failure
        differentUser.setEmail("different@example.com");
        testRendezVous.setUser(differentUser);

        when(rendezVousRepository.findById(1L)).thenReturn(Optional.of(testRendezVous));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                reservationService.addNotes(1L, "notes", testUser));
    }
}
