package com.example.demo.repositories;

import com.example.demo.TestDataFactory;
import com.example.demo.entities.TimeSlot;
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
class TimeSlotRepositoryTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testTimeSlot = TestDataFactory.createTestTimeSlot();
    }

    @Test
    void save_ShouldPersistTimeSlot() {
        // Given
        TimeSlot savedTimeSlot = TestDataFactory.createTestTimeSlot();
        savedTimeSlot.setId(1L);
        
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(savedTimeSlot);

        // When
        TimeSlot saved = timeSlotRepository.save(testTimeSlot);

        // Then
        assertNotNull(saved.getId());
        assertEquals(testTimeSlot.getHeure(), saved.getHeure());
        assertEquals(testTimeSlot.getDate(), saved.getDate());
        assertEquals(testTimeSlot.isDisponible(), saved.isDisponible());
        verify(timeSlotRepository).save(testTimeSlot);
    }

    @Test
    void findById_ShouldReturnTimeSlot_WhenExists() {
        // Given
        TimeSlot savedTimeSlot = TestDataFactory.createTestTimeSlot();
        savedTimeSlot.setId(1L);
        
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(savedTimeSlot);
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(savedTimeSlot));

        // When
        TimeSlot saved = timeSlotRepository.save(testTimeSlot);
        Optional<TimeSlot> found = timeSlotRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getHeure(), found.get().getHeure());
        verify(timeSlotRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        when(timeSlotRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TimeSlot> found = timeSlotRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
        verify(timeSlotRepository).findById(999L);
    }

    @Test
    void findByDateAndDisponibleTrue_ShouldReturnAvailableSlots() {
        // Given
        LocalDate testDate = LocalDate.now().plusDays(1);
        TimeSlot availableSlot = TestDataFactory.createTestTimeSlot();
        availableSlot.setId(1L);
        availableSlot.setDate(testDate);
        availableSlot.setDisponible(true);
        
        when(timeSlotRepository.findByDateAndDisponibleTrue(testDate)).thenReturn(List.of(availableSlot));

        // When
        List<TimeSlot> availableSlots = timeSlotRepository.findByDateAndDisponibleTrue(testDate);

        // Then
        assertEquals(1, availableSlots.size());
        assertTrue(availableSlots.get(0).isDisponible());
        assertEquals(testTimeSlot.getHeure(), availableSlots.get(0).getHeure());
        verify(timeSlotRepository).findByDateAndDisponibleTrue(testDate);
    }

    @Test
    void findByDateOrderByHeureAsc_ShouldReturnOrderedSlots() {
        // Given
        LocalDate testDate = LocalDate.now().plusDays(1);
        
        TimeSlot slot1 = TestDataFactory.createTestTimeSlot();
        slot1.setId(1L);
        slot1.setDate(testDate);
        slot1.setHeure(LocalTime.of(9, 0));

        TimeSlot slot2 = TestDataFactory.createTestTimeSlot();
        slot2.setId(2L);
        slot2.setDate(testDate);
        slot2.setHeure(LocalTime.of(11, 0));

        TimeSlot slot3 = TestDataFactory.createTestTimeSlot();
        slot3.setId(3L);
        slot3.setDate(testDate);
        slot3.setHeure(LocalTime.of(14, 0));
        
        when(timeSlotRepository.findByDateOrderByHeureAsc(testDate)).thenReturn(List.of(slot1, slot2, slot3));

        // When
        List<TimeSlot> slots = timeSlotRepository.findByDateOrderByHeureAsc(testDate);

        // Then
        assertEquals(3, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0).getHeure());
        assertEquals(LocalTime.of(11, 0), slots.get(1).getHeure());
        assertEquals(LocalTime.of(14, 0), slots.get(2).getHeure());
        verify(timeSlotRepository).findByDateOrderByHeureAsc(testDate);
    }

    @Test
    void findByDateAndDisponibleTrueOrderByHeureAsc_ShouldReturnAvailableOrderedSlots() {
        // Given
        LocalDate testDate = LocalDate.now().plusDays(1);
        
        TimeSlot available1 = TestDataFactory.createTestTimeSlot();
        available1.setId(1L);
        available1.setDate(testDate);
        available1.setHeure(LocalTime.of(9, 0));
        available1.setDisponible(true);

        TimeSlot available2 = TestDataFactory.createTestTimeSlot();
        available2.setId(2L);
        available2.setDate(testDate);
        available2.setHeure(LocalTime.of(14, 0));
        available2.setDisponible(true);
        
        when(timeSlotRepository.findByDateAndDisponibleTrueOrderByHeureAsc(testDate)).thenReturn(List.of(available1, available2));

        // When
        List<TimeSlot> slots = timeSlotRepository.findByDateAndDisponibleTrueOrderByHeureAsc(testDate);

        // Then
        assertEquals(2, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0).getHeure());
        assertEquals(LocalTime.of(14, 0), slots.get(1).getHeure());
        assertTrue(slots.stream().allMatch(TimeSlot::isDisponible));
        verify(timeSlotRepository).findByDateAndDisponibleTrueOrderByHeureAsc(testDate);
    }

    @Test
    void deleteByDate_ShouldRemoveAllSlotsForDate() {
        // Given
        LocalDate testDate = LocalDate.now().plusDays(1);
        
        doNothing().when(timeSlotRepository).deleteByDate(testDate);
        when(timeSlotRepository.findByDateOrderByHeureAsc(testDate)).thenReturn(List.of());

        // When
        timeSlotRepository.deleteByDate(testDate);

        // Then
        List<TimeSlot> remainingSlots = timeSlotRepository.findByDateOrderByHeureAsc(testDate);
        assertEquals(0, remainingSlots.size());
        
        verify(timeSlotRepository).deleteByDate(testDate);
        verify(timeSlotRepository).findByDateOrderByHeureAsc(testDate);
    }

    @Test
    void delete_ShouldRemoveTimeSlot() {
        // Given
        TimeSlot savedTimeSlot = TestDataFactory.createTestTimeSlot();
        savedTimeSlot.setId(1L);
        
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(savedTimeSlot);
        doNothing().when(timeSlotRepository).delete(any(TimeSlot.class));

        // When
        TimeSlot saved = timeSlotRepository.save(testTimeSlot);
        timeSlotRepository.delete(saved);

        // Then
        verify(timeSlotRepository).delete(saved);
        verify(timeSlotRepository).save(testTimeSlot);
    }
}
