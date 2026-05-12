package com.example.demo;

import com.example.demo.entities.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class TestDataFactory {

    public static User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(Role.CLIENT);
        user.setEnabled(true);
        return user;
    }

    public static User createTestAdmin() {
        User admin = new User();
        admin.setId(2L);
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setPassword("admin123");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        return admin;
    }

    public static TimeSlot createTestTimeSlot() {
        TimeSlot slot = new TimeSlot();
        slot.setHeure(LocalTime.of(10, 0));
        slot.setDate(LocalDate.now().plusDays(1));
        slot.setDisponible(true);
        return slot;
    }

    public static RendezVous createTestRendezVous(User user, TimeSlot timeSlot) {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setUser(user);
        rendezVous.setTimeSlot(timeSlot);
        rendezVous.setDate(timeSlot.getDate());
        rendezVous.setHeure(timeSlot.getHeure());
        rendezVous.setStatut(Statut.CONFIRMED);
        rendezVous.setNotes("Test appointment notes");
        return rendezVous;
    }

    public static RendezVous createTestRendezVousWithStatus(User user, TimeSlot timeSlot, Statut status) {
        RendezVous rendezVous = createTestRendezVous(user, timeSlot);
        rendezVous.setStatut(status);
        return rendezVous;
    }

    public static WaitingList createTestWaitingList(User user) {
        WaitingList waitingList = new WaitingList();
        waitingList.setUser(user);
        waitingList.setDate(LocalDate.now().plusDays(1));
        waitingList.setPosition(1);
        return waitingList;
    }

    public static BlockedDate createTestBlockedDate() {
        BlockedDate blockedDate = new BlockedDate();
        blockedDate.setDate(LocalDate.now().plusDays(7));
        blockedDate.setReason("Test block reason");
        return blockedDate;
    }

    public static WorkingConfig createTestWorkingConfig() {
        WorkingConfig config = new WorkingConfig();
        config.setDayOfWeek(java.time.DayOfWeek.MONDAY);
        config.setStartTime(LocalTime.of(9, 0));
        config.setEndTime(LocalTime.of(17, 0));
        config.setSlotDurationMinutes(30);
        return config;
    }
}
