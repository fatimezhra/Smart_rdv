package com.example.demo.services;

import com.example.demo.TestDataFactory;
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
import java.util.Arrays;
import java.util.List;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testConfig = TestDataFactory.createTestWorkingConfig();
    }

    @Test
    void generateSlots_ShouldCreateSlots_WhenWorkingConfigExists() {
        // Given
        LocalDate testDate = LocalDate.of(2026, 5, 15);
        
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
        LocalDate testDate = LocalDate.of(2026, 5, 15);
        
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
        LocalDate testDate = LocalDate.of(2026, 5, 15); // Monday
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
        LocalDate testDate = LocalDate.of(2026, 5, 15);
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
        LocalDate testDate = LocalDate.of(2026, 5, 15);
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
        LocalDate testDate = LocalDate.of(2026, 5, 15);
        List<WorkingConfig> configs = Arrays.asList(testConfig);
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
}
