package com.example.demo.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class RendezVousTest {

    @Test
    void testSettersAndGetters() {
        RendezVous rendezVous = new RendezVous();
        User user = new User();
        user.setId(1L);
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        LocalDate date = LocalDate.of(2025, Month.JANUARY, 15);
        LocalTime time = LocalTime.of(10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, Month.JANUARY, 15, 10, 0);

        rendezVous.setId(1L);
        rendezVous.setDate(date);
        rendezVous.setHeure(time);
        rendezVous.setStatut(Statut.CONFIRMED);
        rendezVous.setUser(user);
        rendezVous.setTimeSlot(timeSlot);
        rendezVous.setNotes("Test notes");
        rendezVous.setUpdatedAt(updatedAt);

        assertEquals(1L, rendezVous.getId());
        assertEquals(date, rendezVous.getDate());
        assertEquals(time, rendezVous.getHeure());
        assertEquals(Statut.CONFIRMED, rendezVous.getStatut());
        assertEquals(user, rendezVous.getUser());
        assertEquals(timeSlot, rendezVous.getTimeSlot());
        assertEquals("Test notes", rendezVous.getNotes());
        assertEquals(updatedAt, rendezVous.getUpdatedAt());
    }
}
