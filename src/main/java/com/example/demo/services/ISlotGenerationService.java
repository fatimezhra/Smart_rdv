package com.example.demo.services;

import com.example.demo.entities.BlockedDate;
import com.example.demo.entities.TimeSlot;
import com.example.demo.entities.WorkingConfig;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface ISlotGenerationService {
    WorkingConfig saveWorkingConfig(WorkingConfig config);
    List<WorkingConfig> getAllWorkingConfigs();
    BlockedDate blockDate(LocalDate date, String reason);
    void unblockDate(LocalDate date);
    List<BlockedDate> getBlockedDatesForMonth(YearMonth month);
    boolean isDateBlocked(LocalDate date);
    List<TimeSlot> generateSlotsForDate(LocalDate date);
    List<TimeSlot> getAvailableSlotsForDate(LocalDate date);
    Map<String, String> getCalendarAvailability(YearMonth month);
    List<TimeSlot> generateSlots(LocalDate date);
}
