package com.example.demo.services;

import com.example.demo.TestDataFactory;
import com.example.demo.entities.BlockedDate;
import com.example.demo.entities.TimeSlot;
import com.example.demo.entities.WorkingConfig;
import com.example.demo.repositories.TimeSlotRepository;
import com.example.demo.repositories.WorkingConfigRepository;
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
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotGenerationServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private WorkingConfigRepository workingConfigRepository;

    @Mock
    private BlockedDateRepository blockedDateRepository;

    @InjectMocks
    private SlotGenerationService slotGenerationService;

    private WorkingConfig testConfig;
    private BlockedDate testBlockedDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testConfig = TestDataFactory.createTestWorkingConfig();
        testBlockedDate = new BlockedDate();
        testBlockedDate.setId(1L);
        testBlockedDate.setDate(LocalDate.of(2026, Month.MAY, 15));
        testBlockedDate.setReason("Holiday");
    }

    @Test
    void generateSlots_ShouldCreateSlots_WhenWorkingConfigExists() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);

        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.of(testConfig));
        when(timeSlotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        slotGenerationService.generateSlots(testDate);

        // Then
        verify(timeSlotRepository).saveAll(anyList());
    }

    @Test
    void generateSlots_ShouldThrowException_WhenNoWorkingConfig() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);

        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
                slotGenerationService.generateSlots(testDate));

        verify(timeSlotRepository, never()).deleteByDate(testDate);
        verify(timeSlotRepository, never()).saveAll(anyList());
    }

    @Test
    void generateSlots_ShouldCreateCorrectNumberOfSlots() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15); // Monday
        testConfig.setStartTime(LocalTime.of(9, 0));
        testConfig.setEndTime(LocalTime.of(17, 0));
        testConfig.setSlotDurationMinutes(30);

        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.of(testConfig));

        // When
        slotGenerationService.generateSlots(testDate);

        // Then
        verify(timeSlotRepository).saveAll(argThat(slots -> {
            List<TimeSlot> timeSlots = (List<TimeSlot>) slots;
            // Should create 16 slots: 9:00, 9:30, 10:00, 10:30, 11:00, 11:30, 12:00, 12:30, 13:00, 13:30, 14:00, 14:30, 15:00, 15:30, 16:00, 16:30
            return timeSlots.size() == 16;
        }));
    }

    @Test
    void generateSlots_ShouldHandleBreakTimeCorrectly() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        testConfig.setStartTime(LocalTime.of(9, 0));
        testConfig.setEndTime(LocalTime.of(12, 30));
        testConfig.setSlotDurationMinutes(30);
        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.of(testConfig));

        // When
        slotGenerationService.generateSlots(testDate);

        // Then
        verify(timeSlotRepository).saveAll(argThat(slots -> {
            List<TimeSlot> timeSlots = (List<TimeSlot>) slots;
            // Should create 7 slots: 9:00, 9:30, 10:00, 10:30, 11:00, 11:30, 12:00, 12:30
            return timeSlots.size() == 7;
        }));
    }

    @Test
    void generateSlots_ShouldHandleMultipleWorkingConfigs() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        WorkingConfig config1 = TestDataFactory.createTestWorkingConfig();
        config1.setStartTime(LocalTime.of(9, 0));
        config1.setEndTime(LocalTime.of(12, 0));
        config1.setSlotDurationMinutes(30);

        WorkingConfig config2 = TestDataFactory.createTestWorkingConfig();
        config2.setStartTime(LocalTime.of(14, 0));
        config2.setEndTime(LocalTime.of(17, 0));
        config2.setSlotDurationMinutes(60);

        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.of(config2));

        // When
        slotGenerationService.generateSlots(testDate);

        // Then
        verify(timeSlotRepository).saveAll(argThat(slots -> {
            List<TimeSlot> timeSlots = (List<TimeSlot>) slots;
            // Should create 3 slots from config2 (14:00, 15:00, 16:00)
            return timeSlots.size() == 3;
        }));
    }

    @Test
    void generateSlots_ShouldSetCorrectDateAndAvailability() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.of(testConfig));

        // When
        slotGenerationService.generateSlots(testDate);

        // Then
        verify(timeSlotRepository).saveAll(argThat(slots -> {
            List<TimeSlot> timeSlots = (List<TimeSlot>) slots;
            return timeSlots.stream().allMatch(slot ->
                slot.getDate().equals(testDate) &&
                slot.isDisponible());
        }));
    }

    @Test
    void getAvailableSlotsForDate_ShouldReturnAvailableSlots() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        TimeSlot slot1 = TestDataFactory.createTestTimeSlot();
        slot1.setDisponible(true);
        TimeSlot slot2 = TestDataFactory.createTestTimeSlot();
        slot2.setDisponible(false);

        when(timeSlotRepository.findByDateAndDisponibleTrue(testDate)).thenReturn(List.of(slot1));

        // When
        List<TimeSlot> result = slotGenerationService.getAvailableSlotsForDate(testDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isDisponible());
    }

    @Test
    void getAvailableSlotsForDate_ShouldReturnEmptyList_WhenNoSlotsAvailable() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(timeSlotRepository.findByDateAndDisponibleTrue(testDate)).thenReturn(List.of());

        // When
        List<TimeSlot> result = slotGenerationService.getAvailableSlotsForDate(testDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCalendarAvailability_ShouldReturnAvailabilityMap() {
        // Given
        java.time.YearMonth month = java.time.YearMonth.of(2026, Month.MAY);
        when(timeSlotRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        // When
        java.util.Map<String, String> result = slotGenerationService.getCalendarAvailability(month);

        // Then
        assertNotNull(result);
    }

    @Test
    void generateSlots_ShouldDeleteExistingSlots_BeforeCreatingNew() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.of(testConfig));

        // When
        slotGenerationService.generateSlots(testDate);

        // Then
        verify(timeSlotRepository).deleteByDate(testDate);
        verify(timeSlotRepository).saveAll(anyList());
    }

    @Test
    void generateSlotsForDate_ShouldReturnSlots_WhenSuccessful() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(false);
        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.of(testConfig));
        when(timeSlotRepository.findByDate(testDate)).thenReturn(List.of());
        when(timeSlotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<TimeSlot> result = slotGenerationService.generateSlotsForDate(testDate);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(timeSlotRepository).saveAll(anyList());
    }

    @Test
    void generateSlotsForDate_ShouldThrowException_WhenDateIsBlocked() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(true);

        // When & Then
        assertThrows(com.example.demo.exceptions.BadRequestException.class, () ->
                slotGenerationService.generateSlotsForDate(testDate));

        verify(timeSlotRepository, never()).saveAll(anyList());
    }

    @Test
    void generateSlotsForDate_ShouldThrowException_WhenNoWorkingConfigExists() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(false);
        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThrows(com.example.demo.exceptions.ResourceNotFoundException.class, () ->
                slotGenerationService.generateSlotsForDate(testDate));

        verify(timeSlotRepository, never()).saveAll(anyList());
    }

    @Test
    void generateSlotsForDate_ShouldReturnExistingSlots_WhenSlotsAlreadyExist() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        TimeSlot existingSlot = TestDataFactory.createTestTimeSlot();
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(false);
        when(workingConfigRepository.findByDayOfWeek(any())).thenReturn(java.util.Optional.of(testConfig));
        when(timeSlotRepository.findByDate(testDate)).thenReturn(List.of(existingSlot));

        // When
        List<TimeSlot> result = slotGenerationService.generateSlotsForDate(testDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(timeSlotRepository, never()).saveAll(anyList());
    }

    @Test
    void getAvailableSlotsForDate_ShouldReturnEmptyList_WhenDateIsBlocked() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(true);

        // When
        List<TimeSlot> result = slotGenerationService.getAvailableSlotsForDate(testDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(timeSlotRepository, never()).findByDateAndDisponibleTrue(testDate);
    }

    @Test
    void saveWorkingConfig_ShouldThrowException_WhenEndTimeBeforeStartTime() {
        // Given
        testConfig.setStartTime(LocalTime.of(17, 0));
        testConfig.setEndTime(LocalTime.of(9, 0));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                slotGenerationService.saveWorkingConfig(testConfig));
    }

    @Test
    void saveWorkingConfig_ShouldThrowException_WhenSlotDurationInvalid() {
        // Given
        testConfig.setSlotDurationMinutes(0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                slotGenerationService.saveWorkingConfig(testConfig));
    }

    @Test
    void blockDate_ShouldThrowException_WhenDateAlreadyBlocked() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(true);

        // When & Then
        assertThrows(com.example.demo.exceptions.BadRequestException.class, () ->
                slotGenerationService.blockDate(testDate, "Holiday"));
    }

    @Test
    void unblockDate_ShouldThrowException_WhenDateNotBlocked() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.findByDate(testDate)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThrows(com.example.demo.exceptions.ResourceNotFoundException.class, () ->
                slotGenerationService.unblockDate(testDate));
    }

    @Test
    void isDateBlocked_ShouldReturnTrue_WhenDateIsBlocked() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(true);

        // When
        boolean result = slotGenerationService.isDateBlocked(testDate);

        // Then
        assertTrue(result);
    }

    @Test
    void isDateBlocked_ShouldReturnFalse_WhenDateIsNotBlocked() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(false);

        // When
        boolean result = slotGenerationService.isDateBlocked(testDate);

        // Then
        assertFalse(result);
    }

    @Test
    void saveWorkingConfig_ShouldThrowException_WhenSlotDurationEquals481() {
        // Given
        testConfig.setSlotDurationMinutes(481);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                slotGenerationService.saveWorkingConfig(testConfig));
    }

    @Test
    void blockDate_ShouldBlockDate_WhenNotAlreadyBlocked() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(false);
        when(blockedDateRepository.save(any(BlockedDate.class))).thenReturn(testBlockedDate);

        // When
        BlockedDate result = slotGenerationService.blockDate(testDate, "Holiday");

        // Then
        assertNotNull(result);
        verify(blockedDateRepository).save(any(BlockedDate.class));
    }

    @Test
    void getBlockedDatesForMonth_ShouldReturnEmptyList_WhenNoBlockedDates() {
        // Given
        YearMonth month = YearMonth.of(2026, Month.MAY);
        when(blockedDateRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        // When
        List<BlockedDate> result = slotGenerationService.getBlockedDatesForMonth(month);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCalendarAvailability_ShouldReturnBlockedStatus_WhenDateIsBlocked() {
        // Given
        YearMonth month = YearMonth.of(2026, Month.MAY);
        BlockedDate blocked = new BlockedDate();
        blocked.setDate(LocalDate.of(2026, Month.MAY, 15));
        blocked.setReason("Holiday");
        when(blockedDateRepository.findByDateBetween(any(), any())).thenReturn(List.of(blocked));
        when(workingConfigRepository.findAll()).thenReturn(List.of());
        when(timeSlotRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        // When
        Map<String, String> result = slotGenerationService.getCalendarAvailability(month);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("2026-05-15"));
        assertTrue(result.get("2026-05-15").startsWith("BLOCKED:"));
    }

    @Test
    void getCalendarAvailability_ShouldReturnNonWorkingStatus_WhenNoConfig() {
        // Given
        YearMonth month = YearMonth.of(2026, Month.MAY);
        when(blockedDateRepository.findByDateBetween(any(), any())).thenReturn(List.of());
        when(workingConfigRepository.findAll()).thenReturn(List.of());
        when(timeSlotRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        // When
        Map<String, String> result = slotGenerationService.getCalendarAvailability(month);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("2026-05-15"));
        assertEquals("NON_WORKING", result.get("2026-05-15"));
    }

    @Test
    void getCalendarAvailability_ShouldReturnNoSlotsStatus_WhenNoSlots() {
        // Given
        YearMonth month = YearMonth.of(2026, Month.MAY);
        testConfig.setDayOfWeek(java.time.DayOfWeek.FRIDAY);
        when(blockedDateRepository.findByDateBetween(any(), any())).thenReturn(List.of());
        when(workingConfigRepository.findAll()).thenReturn(List.of(testConfig));
        when(timeSlotRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        // When
        Map<String, String> result = slotGenerationService.getCalendarAvailability(month);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("2026-05-15"));
        assertEquals("NO_SLOTS", result.get("2026-05-15"));
    }

    @Test
    void getCalendarAvailability_ShouldReturnFullStatus_WhenNoAvailableSlots() {
        // Given
        YearMonth month = YearMonth.of(2026, Month.MAY);
        TimeSlot slot = TestDataFactory.createTestTimeSlot();
        slot.setDisponible(false);
        slot.setDate(LocalDate.of(2026, Month.MAY, 15));
        testConfig.setDayOfWeek(java.time.DayOfWeek.FRIDAY);
        when(blockedDateRepository.findByDateBetween(any(), any())).thenReturn(List.of());
        when(workingConfigRepository.findAll()).thenReturn(List.of(testConfig));
        when(timeSlotRepository.findByDateBetween(any(), any())).thenReturn(List.of(slot));

        // When
        Map<String, String> result = slotGenerationService.getCalendarAvailability(month);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("2026-05-15"));
        assertEquals("FULL", result.get("2026-05-15"));
    }

    @Test
    void generateSlots_ShouldThrowException_WhenDateIsBlocked() {
        // Given
        LocalDate testDate = LocalDate.of(2026, Month.MAY, 15);
        when(blockedDateRepository.existsByDate(testDate)).thenReturn(true);

        // When & Then
        assertThrows(com.example.demo.exceptions.BadRequestException.class, () ->
                slotGenerationService.generateSlots(testDate));

        verify(timeSlotRepository, never()).deleteByDate(testDate);
        verify(timeSlotRepository, never()).saveAll(anyList());
    }
}
