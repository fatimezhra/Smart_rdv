package com.example.demo.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {

    @Test
    void testConstructor() {
        BadRequestException exception = new BadRequestException("Bad request message");
        assertEquals("Bad request message", exception.getMessage());
    }

    @Test
    void testIsRuntimeException() {
        BadRequestException exception = new BadRequestException("Test");
        assertTrue(exception instanceof RuntimeException);
    }
}
