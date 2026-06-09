package com.example.demo.services;

import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PdfServiceTest {

    private PdfService pdfService;
    private RendezVous testRendezVous;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService();

        // Create test user
        User testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");

        // Create test appointment
        testRendezVous = new RendezVous();
        testRendezVous.setUser(testUser);
        testRendezVous.setDate(LocalDate.of(2026, Month.MAY, 15));
        testRendezVous.setHeure(LocalTime.of(10, 30));
        testRendezVous.setStatut(Statut.CONFIRMED);
        testRendezVous.setNotes("Regular checkup");
    }

    @Test
    void testGenerateAppointmentPdf() throws Exception {
        byte[] pdfBytes = pdfService.generateAppointmentPdf(testRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Verify PDF header
        String pdfHeader = new String(pdfBytes, 0, 4);
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    void testGenerateAppointmentPdfWithNullValues() throws Exception {
        RendezVous nullRendezVous = new RendezVous();

        User testUser = new User();
        testUser.setName("Jane Smith");
        nullRendezVous.setUser(testUser);

        byte[] pdfBytes = pdfService.generateAppointmentPdf(nullRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
