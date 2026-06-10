package com.example.demo.services;

import com.example.demo.entities.BlockedDate;
import com.example.demo.entities.TimeSlot;
import com.example.demo.entities.WorkingConfig;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.BlockedDateRepository;
import com.example.demo.repositories.TimeSlotRepository;
import com.example.demo.repositories.WorkingConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class SlotGenerationService implements ISlotGenerationService {

    @Autowired
    private WorkingConfigRepository workingConfigRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private BlockedDateRepository blockedDateRepository;

    public WorkingConfig saveWorkingConfig(WorkingConfig config) {
        // Fix 6: Validation for working hours
        if (!config.getEndTime().isAfter(config.getStartTime())) {
            throw new IllegalArgumentException("L'heure de fin doit être après l'heure de début.");
        }
        if (config.getSlotDurationMinutes() <= 0 || config.getSlotDurationMinutes() > 480) {
            throw new IllegalArgumentException("La durée d'un créneau doit être entre 1 et 480 minutes.");
        }
        return workingConfigRepository.save(config);
    }

    public List<WorkingConfig> getAllWorkingConfigs() {
        return workingConfigRepository.findAll();
    }

    public BlockedDate blockDate(LocalDate date, String reason) {
        if (blockedDateRepository.existsByDate(date)) {
            throw new BadRequestException("Date already blocked");
        }
        BlockedDate blocked = new BlockedDate();
        blocked.setDate(date);
        blocked.setReason(reason);
        return blockedDateRepository.save(blocked);
    }

    public void unblockDate(LocalDate date) {
        BlockedDate blocked = blockedDateRepository.findByDate(date)
                .orElseThrow(() -> new ResourceNotFoundException("Date not blocked"));
        blockedDateRepository.delete(blocked);
    }

    public List<BlockedDate> getBlockedDatesForMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return blockedDateRepository.findByDateBetween(start, end);
    }

    public boolean isDateBlocked(LocalDate date) {
        return blockedDateRepository.existsByDate(date);
    }

    @Transactional
    public List<TimeSlot> generateSlotsForDate(LocalDate date) {
        if (isDateBlocked(date)) {
            throw new BadRequestException("Cannot generate slots for a blocked date");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        WorkingConfig config = workingConfigRepository.findByDayOfWeek(dayOfWeek)
                .orElseThrow(() -> new ResourceNotFoundException("No working config for " + dayOfWeek));

        List<TimeSlot> existing = timeSlotRepository.findByDate(date);
        if (!existing.isEmpty()) {
            return existing;
        }

        List<TimeSlot> slots = new ArrayList<>();
        LocalTime current = config.getStartTime();
        while (current.isBefore(config.getEndTime())) {
            TimeSlot slot = new TimeSlot();
            slot.setDate(date);
            slot.setHeure(current);
            slot.setDisponible(true);
            slots.add(slot);
            current = current.plusMinutes(config.getSlotDurationMinutes());
        }

        return timeSlotRepository.saveAll(slots);
    }

    public List<TimeSlot> getAvailableSlotsForDate(LocalDate date) {
        if (isDateBlocked(date)) {
            return Collections.emptyList();
        }
        return timeSlotRepository.findByDateAndDisponibleTrue(date);
    }

    public Map<String, String> getCalendarAvailability(YearMonth month) {
        Map<String, String> result = new LinkedHashMap<>();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        // Batch fetch all data needed for the month
        List<BlockedDate> blockedDates = blockedDateRepository.findByDateBetween(start, end);
        List<WorkingConfig> workingConfigs = workingConfigRepository.findAll();
        List<TimeSlot> allSlots = timeSlotRepository.findByDateBetween(start, end);

        // Create lookup maps for fast access
        Map<LocalDate, String> blockedDateMap = new HashMap<>();
        for (BlockedDate bd : blockedDates) {
            blockedDateMap.put(bd.getDate(), bd.getReason());
        }

        Map<DayOfWeek, WorkingConfig> workingConfigMap = new EnumMap<>(DayOfWeek.class);
        for (WorkingConfig wc : workingConfigs) {
            workingConfigMap.put(wc.getDayOfWeek(), wc);
        }

        Map<LocalDate, List<TimeSlot>> slotsByDate = new HashMap<>();
        for (TimeSlot slot : allSlots) {
            slotsByDate.computeIfAbsent(slot.getDate(), k -> new ArrayList<>()).add(slot);
        }

        // Compute availability for each day
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String status = computeDateStatus(date, blockedDateMap, workingConfigMap, slotsByDate);
            result.put(date.toString(), status);
        }
        return result;
    }

    private String computeDateStatus(LocalDate date, Map<LocalDate, String> blockedDateMap,
                                      Map<DayOfWeek, WorkingConfig> workingConfigMap,
                                      Map<LocalDate, List<TimeSlot>> slotsByDate) {
        if (blockedDateMap.containsKey(date)) {
            return "BLOCKED:" + blockedDateMap.get(date);
        }
        if (!workingConfigMap.containsKey(date.getDayOfWeek())) {
            return "NON_WORKING";
        }
        List<TimeSlot> daySlots = slotsByDate.get(date);
        if (daySlots == null || daySlots.isEmpty()) {
            return "NO_SLOTS";
        }
        long available = daySlots.stream().filter(TimeSlot::isDisponible).count();
        if (available == 0) {
            return "FULL";
        }
        return "OPEN";
    }

    // ===================== GENERATE SLOTS =====================
    @Transactional
    public List<TimeSlot> generateSlots(LocalDate date) {
        if (isDateBlocked(date)) {
            throw new BadRequestException("Cannot generate slots for a blocked date");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        WorkingConfig config = workingConfigRepository.findByDayOfWeek(dayOfWeek)
                .orElseThrow(() -> new ResourceNotFoundException("No working config for " + dayOfWeek));

        // Delete existing slots for the date
        timeSlotRepository.deleteByDate(date);

        List<TimeSlot> slots = new ArrayList<>();
        LocalTime current = config.getStartTime();
        while (current.isBefore(config.getEndTime())) {
            TimeSlot slot = new TimeSlot();
            slot.setDate(date);
            slot.setHeure(current);
            slot.setDisponible(true);
            slots.add(slot);
            current = current.plusMinutes(config.getSlotDurationMinutes());
        }

        return timeSlotRepository.saveAll(slots);
    }
}
