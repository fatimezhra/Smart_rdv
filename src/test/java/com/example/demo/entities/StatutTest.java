package com.example.demo.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatutTest {

    @Test
    void testStatutValues() {
        assertEquals("CONFIRMED", Statut.CONFIRMED.name());
        assertEquals("CANCELLED", Statut.CANCELLED.name());
        assertEquals("WAITING", Statut.WAITING.name());
    }
}
