package com.example.demo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class BlockedDateDTOTest {

    @Test
    void testNoArgsConstructor() {
        BlockedDateDTO dto = new BlockedDateDTO();
        assertNotNull(dto);
    }

    @Test
    void testSettersAndGetters() {
        BlockedDateDTO dto = new BlockedDateDTO();
        LocalDate date = LocalDate.of(2025, Month.JANUARY, 15);
        dto.setDate(date);
        dto.setReason("Holiday");

        assertEquals(date, dto.getDate());
        assertEquals("Holiday", dto.getReason());
    }
}
