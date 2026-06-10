package com.example.demo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void testNoArgsConstructor() {
        AuthResponse response = new AuthResponse();
        assertNotNull(response);
    }

    @Test
    void testAllArgsConstructor() {
        AuthResponse response = new AuthResponse("token123", "CLIENT", "test@example.com", "Test User");
        assertEquals("token123", response.getToken());
        assertEquals("CLIENT", response.getRole());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
    }

    @Test
    void testSettersAndGetters() {
        AuthResponse response = new AuthResponse();
        response.setToken("token456");
        response.setRole("ADMIN");
        response.setEmail("admin@example.com");
        response.setName("Admin User");

        assertEquals("token456", response.getToken());
        assertEquals("ADMIN", response.getRole());
        assertEquals("admin@example.com", response.getEmail());
        assertEquals("Admin User", response.getName());
    }
}
