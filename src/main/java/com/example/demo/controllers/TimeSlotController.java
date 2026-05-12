package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.TimeSlot;
import com.example.demo.repositories.TimeSlotRepository;
import com.example.demo.services.SlotGenerationService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
public class TimeSlotController {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private SlotGenerationService slotGenerationService;

    @GetMapping("/timeslots")
    public List<TimeSlot> getAvailableSlots(@RequestParam(required = false) String date) {
        if (date != null && !date.isEmpty()) {
            LocalDate localDate = LocalDate.parse(date);
            return timeSlotRepository.findByDateAndDisponibleTrue(localDate);
        }
        return timeSlotRepository.findAll();
    }

    @GetMapping("/api/slots/available")
    public List<TimeSlot> getSlotsForDate(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return slotGenerationService.getAvailableSlotsForDate(localDate);
    }

    @GetMapping("/api/slots/calendar")
    public Map<String, String> getCalendarAvailability(@RequestParam String month) {
        YearMonth ym = YearMonth.parse(month);
        return slotGenerationService.getCalendarAvailability(ym);
    }

    @GetMapping("/api/slots/all")
    public List<TimeSlot> getAllSlotsForDate(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return timeSlotRepository.findByDate(localDate);
    }
}
