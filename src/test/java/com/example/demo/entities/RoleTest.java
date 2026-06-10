package com.example.demo.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testRoleValues() {
        assertEquals("CLIENT", Role.CLIENT.name());
        assertEquals("ADMIN", Role.ADMIN.name());
    }
}
