package com.example.demo.controllers;

import com.example.demo.dto.BlockedDateDTO;
import com.example.demo.dto.WorkingConfigDTO;
import com.example.demo.entities.BlockedDate;
import com.example.demo.entities.TimeSlot;
import com.example.demo.entities.WorkingConfig;
import com.example.demo.services.ISlotGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotAdminControllerTest {

    @Mock
    private ISlotGenerationService slotGenerationService;

    @InjectMocks
    private SlotAdminController slotAdminController;

    private WorkingConfig testConfig;
    private BlockedDate testBlockedDate;
    private TimeSlot testSlot;

    @BeforeEach
    void setUp() {
        testConfig = new WorkingConfig();
        testConfig.setId(1L);
        testConfig.setDayOfWeek(DayOfWeek.MONDAY);
        testConfig.setStartTime(LocalTime.of(9, 0));
        testConfig.setEndTime(LocalTime.of(17, 0));
        testConfig.setSlotDurationMinutes(30);

        testBlockedDate = new BlockedDate();
        testBlockedDate.setId(1L);
        testBlockedDate.setDate(LocalDate.of(2025, java.time.Month.JANUARY, 15));
        testBlockedDate.setReason("Holiday");

        testSlot = new TimeSlot();
        testSlot.setId(1L);
        testSlot.setDate(LocalDate.of(2025, java.time.Month.JANUARY, 15));
        testSlot.setHeure(LocalTime.of(10, 0));
        testSlot.setDisponible(true);
    }

    @Test
    void generateSlots_ShouldReturnGeneratedSlots() {
        List<TimeSlot> slots = Arrays.asList(testSlot);
        when(slotGenerationService.generateSlotsForDate(any(LocalDate.class))).thenReturn(slots);

        List<TimeSlot> result = slotAdminController.generateSlots("2025-01-15");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(slotGenerationService).generateSlotsForDate(any(LocalDate.class));
    }

    @Test
    void setWorkingHours_ShouldSaveWorkingConfig() {
        WorkingConfigDTO dto = new WorkingConfigDTO(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                30
        );
        when(slotGenerationService.saveWorkingConfig(any(WorkingConfig.class))).thenReturn(testConfig);

        WorkingConfig result = slotAdminController.setWorkingHours(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(slotGenerationService).saveWorkingConfig(any(WorkingConfig.class));
    }

    @Test
    void getWorkingHours_ShouldReturnAllWorkingConfigs() {
        List<WorkingConfig> configs = Arrays.asList(testConfig);
        when(slotGenerationService.getAllWorkingConfigs()).thenReturn(configs);

        List<WorkingConfig> result = slotAdminController.getWorkingHours();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(slotGenerationService).getAllWorkingConfigs();
    }

    @Test
    void blockDate_ShouldBlockDate() {
        BlockedDateDTO dto = new BlockedDateDTO();
        dto.setDate(LocalDate.of(2025, java.time.Month.JANUARY, 15));
        dto.setReason("Holiday");
        when(slotGenerationService.blockDate(any(LocalDate.class), anyString())).thenReturn(testBlockedDate);

        BlockedDate result = slotAdminController.blockDate(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(slotGenerationService).blockDate(any(LocalDate.class), anyString());
    }

    @Test
    void unblockDate_ShouldUnblockDate() {
        doNothing().when(slotGenerationService).unblockDate(any(LocalDate.class));

        slotAdminController.unblockDate("2025-01-15");

        verify(slotGenerationService).unblockDate(any(LocalDate.class));
    }

    @Test
    void getBlockedDates_WithoutMonth_ShouldReturnCurrentMonthBlockedDates() {
        List<BlockedDate> blockedDates = Arrays.asList(testBlockedDate);
        when(slotGenerationService.getBlockedDatesForMonth(any(YearMonth.class))).thenReturn(blockedDates);

        List<BlockedDate> result = slotAdminController.getBlockedDates(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(slotGenerationService).getBlockedDatesForMonth(any(YearMonth.class));
    }

    @Test
    void getBlockedDates_WithMonth_ShouldReturnSpecifiedMonthBlockedDates() {
        List<BlockedDate> blockedDates = Arrays.asList(testBlockedDate);
        when(slotGenerationService.getBlockedDatesForMonth(any(YearMonth.class))).thenReturn(blockedDates);

        List<BlockedDate> result = slotAdminController.getBlockedDates("2025-01");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(slotGenerationService).getBlockedDatesForMonth(any(YearMonth.class));
    }
}
