package com.example.demo.entities;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkingConfigTest {

    @Test
    void testSettersAndGetters() {
        WorkingConfig config = new WorkingConfig();
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        config.setDayOfWeek(day);
        config.setStartTime(startTime);
        config.setEndTime(endTime);
        config.setSlotDurationMinutes(30);

        assertEquals(day, config.getDayOfWeek());
        assertEquals(startTime, config.getStartTime());
        assertEquals(endTime, config.getEndTime());
        assertEquals(30, config.getSlotDurationMinutes());
    }
}
