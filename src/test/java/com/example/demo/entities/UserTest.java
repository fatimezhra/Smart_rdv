package com.example.demo.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testSettersAndGetters() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(Role.CLIENT);
        user.setEnabled(true);

        assertEquals(1L, user.getId());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals(Role.CLIENT, user.getRole());
        assertTrue(user.isEnabled());
    }

    @Test
    void testDefaultEnabled() {
        User user = new User();
        assertTrue(user.isEnabled());
    }
}
