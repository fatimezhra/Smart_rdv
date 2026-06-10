package com.example.demo.services;

import com.example.demo.entities.RendezVous;
import com.example.demo.entities.TimeSlot;
import com.example.demo.entities.User;
import com.example.demo.entities.WaitingList;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.TimeSlotRepository;
import com.example.demo.repositories.WaitingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WaitingListAdminServiceTest {

    @Mock
    private WaitingListRepository waitingListRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private IReservationService reservationService;

    @InjectMocks
    private WaitingListAdminService waitingListAdminService;

    private WaitingList testWaitingList;
    private User testUser;
    private TimeSlot testSlot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testSlot = new TimeSlot();
        testSlot.setId(1L);
        testSlot.setDate(LocalDate.of(2025, Month.JANUARY, 15).plusDays(1));
        testSlot.setHeure(LocalTime.of(10, 0));
        testSlot.setDisponible(true);

        testWaitingList = new WaitingList();
        testWaitingList.setId(1L);
        testWaitingList.setUser(testUser);
        testWaitingList.setDate(LocalDate.of(2025, Month.JANUARY, 15).plusDays(1));
        testWaitingList.setPosition(1);
    }

    @Test
    void getFullWaitingList_ShouldReturnAllWaitingListEntries() {
        // Given
        List<WaitingList> waitingList = List.of(testWaitingList);
        when(waitingListRepository.findAllByOrderByDateAscPositionAsc()).thenReturn(waitingList);

        // When
        List<WaitingList> result = waitingListAdminService.getFullWaitingList();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWaitingList.getId(), result.get(0).getId());
        verify(waitingListRepository).findAllByOrderByDateAscPositionAsc();
    }

    @Test
    void promoteWaitingListEntry_ShouldPromoteAndCreateReservation_WhenSlotAvailable() {
        // Given
        when(waitingListRepository.findById(1L)).thenReturn(Optional.of(testWaitingList));
        when(timeSlotRepository.findByDateAndDisponibleTrue(testWaitingList.getDate())).thenReturn(List.of(testSlot));
        doReturn(ResponseEntity.ok(new RendezVous())).when(reservationService).reserver(testSlot.getId(), testUser);
        when(waitingListRepository.findByDateOrderByPositionAsc(testWaitingList.getDate())).thenReturn(List.of());
        doNothing().when(waitingListRepository).delete(testWaitingList);
        when(waitingListRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        waitingListAdminService.promoteWaitingListEntry(1L);

        // Then
        verify(waitingListRepository).findById(1L);
        verify(timeSlotRepository).findByDateAndDisponibleTrue(testWaitingList.getDate());
        verify(reservationService).reserver(testSlot.getId(), testUser);
        verify(waitingListRepository).delete(testWaitingList);
        verify(waitingListRepository).saveAll(anyList());
    }

    @Test
    void promoteWaitingListEntry_ShouldThrowException_WhenEntryNotFound() {
        // Given
        when(waitingListRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> waitingListAdminService.promoteWaitingListEntry(999L));
        verify(waitingListRepository).findById(999L);
        verify(timeSlotRepository, never()).findByDateAndDisponibleTrue(any());
        verify(reservationService, never()).reserver(anyLong(), any());
    }

    @Test
    void promoteWaitingListEntry_ShouldThrowException_WhenNoSlotsAvailable() {
        // Given
        when(waitingListRepository.findById(1L)).thenReturn(Optional.of(testWaitingList));
        when(timeSlotRepository.findByDateAndDisponibleTrue(testWaitingList.getDate())).thenReturn(List.of());

        // When & Then
        assertThrows(BadRequestException.class, () -> waitingListAdminService.promoteWaitingListEntry(1L));
        verify(waitingListRepository).findById(1L);
        verify(timeSlotRepository).findByDateAndDisponibleTrue(testWaitingList.getDate());
        verify(reservationService, never()).reserver(anyLong(), any());
    }

    @Test
    void removeFromWaitingList_ShouldRemoveEntryAndRecalculatePositions() {
        // Given
        when(waitingListRepository.findById(1L)).thenReturn(Optional.of(testWaitingList));
        when(waitingListRepository.findByDateOrderByPositionAsc(testWaitingList.getDate())).thenReturn(List.of());
        doNothing().when(waitingListRepository).delete(testWaitingList);
        when(waitingListRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        waitingListAdminService.removeFromWaitingList(1L);

        // Then
        verify(waitingListRepository).findById(1L);
        verify(waitingListRepository).delete(testWaitingList);
        verify(waitingListRepository).findByDateOrderByPositionAsc(testWaitingList.getDate());
        verify(waitingListRepository).saveAll(anyList());
    }

    @Test
    void removeFromWaitingList_ShouldThrowException_WhenEntryNotFound() {
        // Given
        when(waitingListRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> waitingListAdminService.removeFromWaitingList(999L));
        verify(waitingListRepository).findById(999L);
        verify(waitingListRepository, never()).delete(any());
    }

    @Test
    void removeFromWaitingList_ShouldRecalculatePositions_WhenMultipleEntriesExist() {
        // Given
        WaitingList entry2 = new WaitingList();
        entry2.setId(2L);
        entry2.setPosition(2);

        when(waitingListRepository.findById(1L)).thenReturn(Optional.of(testWaitingList));
        when(waitingListRepository.findByDateOrderByPositionAsc(testWaitingList.getDate())).thenReturn(List.of(entry2));
        doNothing().when(waitingListRepository).delete(testWaitingList);
        when(waitingListRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        waitingListAdminService.removeFromWaitingList(1L);

        // Then
        verify(waitingListRepository).saveAll(argThat(list -> {
            List<WaitingList> entries = (List<WaitingList>) list;
            return entries.get(0).getPosition() == 1;
        }));
    }
}
