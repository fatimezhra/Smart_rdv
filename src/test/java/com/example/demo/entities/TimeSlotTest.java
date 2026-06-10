package com.example.demo.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    @Test
    void testSettersAndGetters() {
        TimeSlot timeSlot = new TimeSlot();
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);

        timeSlot.setDate(date);
        timeSlot.setHeure(time);
        timeSlot.setDisponible(true);

        assertEquals(date, timeSlot.getDate());
        assertEquals(time, timeSlot.getHeure());
        assertTrue(timeSlot.isDisponible());
    }
}
