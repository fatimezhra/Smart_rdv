package com.example.demo.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class BlockedDateTest {

    @Test
    void testSettersAndGetters() {
        BlockedDate blockedDate = new BlockedDate();
        LocalDate date = LocalDate.of(2025, Month.JANUARY, 15);
        blockedDate.setDate(date);
        blockedDate.setReason("Holiday");

        assertEquals(date, blockedDate.getDate());
        assertEquals("Holiday", blockedDate.getReason());
    }
}
