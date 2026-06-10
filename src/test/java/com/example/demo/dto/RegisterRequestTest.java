package com.example.demo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    @Test
    void testNoArgsConstructor() {
        RegisterRequest request = new RegisterRequest();
        assertNotNull(request);
    }

    @Test
    void testSettersAndGetters() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        assertEquals("Test User", request.getName());
        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }
}
