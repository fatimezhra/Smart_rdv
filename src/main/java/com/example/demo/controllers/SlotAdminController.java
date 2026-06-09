package com.example.demo.controllers;

import com.example.demo.dto.BlockedDateDTO;
import com.example.demo.dto.WorkingConfigDTO;
import com.example.demo.entities.BlockedDate;
import com.example.demo.entities.TimeSlot;
import com.example.demo.entities.WorkingConfig;
import com.example.demo.services.SlotGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class SlotAdminController {

    private static final ZoneId ZONE_ID = ZoneId.of("Africa/Casablanca");

    @Autowired
    private SlotGenerationService slotGenerationService;

    @GetMapping("/slots/generate")
    public List<TimeSlot> generateSlots(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return slotGenerationService.generateSlotsForDate(localDate);
    }

    @PostMapping("/config/hours")
    public WorkingConfig setWorkingHours(@RequestBody WorkingConfigDTO configDTO) {
        WorkingConfig config = new WorkingConfig();
        config.setDayOfWeek(configDTO.dayOfWeek());
        config.setStartTime(configDTO.startTime());
        config.setEndTime(configDTO.endTime());
        config.setSlotDurationMinutes(configDTO.slotDurationMinutes());
        return slotGenerationService.saveWorkingConfig(config);
    }

    @GetMapping("/config/hours")
    public List<WorkingConfig> getWorkingHours() {
        return slotGenerationService.getAllWorkingConfigs();
    }

    @PostMapping("/blocked-dates")
    public BlockedDate blockDate(@RequestBody BlockedDateDTO blockedDateDTO) {
        return slotGenerationService.blockDate(blockedDateDTO.getDate(), blockedDateDTO.getReason());
    }

    @DeleteMapping("/blocked-dates/{date}")
    public void unblockDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        slotGenerationService.unblockDate(localDate);
    }

    @GetMapping("/blocked-dates")
    public List<BlockedDate> getBlockedDates(@RequestParam(required = false) String month) {
        if (month != null && !month.isEmpty()) {
            YearMonth ym = YearMonth.parse(month);
            return slotGenerationService.getBlockedDatesForMonth(ym);
        }
        return slotGenerationService.getBlockedDatesForMonth(YearMonth.now(ZONE_ID));
    }
}
