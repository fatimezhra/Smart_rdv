package com.example.demo.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class WaitingListTest {

    @Test
    void testSettersAndGetters() {
        WaitingList waitingList = new WaitingList();
        LocalDate date = LocalDate.of(2025, Month.JANUARY, 15);
        User user = new User();
        user.setId(1L);

        waitingList.setDate(date);
        waitingList.setUser(user);
        waitingList.setPosition(1);

        assertEquals(date, waitingList.getDate());
        assertEquals(user, waitingList.getUser());
        assertEquals(1, waitingList.getPosition());
    }
}
