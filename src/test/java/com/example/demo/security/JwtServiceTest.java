package com.example.demo.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void testImplementsIJwtService() {
        JwtService jwtService = new JwtService();
        assertTrue(jwtService instanceof IJwtService);
    }
}
