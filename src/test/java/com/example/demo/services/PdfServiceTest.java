package com.example.demo.services;

import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
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

    @Test
    void testGenerateAppointmentPdfWithCancelledStatus() throws Exception {
        testRendezVous.setStatut(Statut.CANCELLED);

        byte[] pdfBytes = pdfService.generateAppointmentPdf(testRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGenerateAppointmentPdfWithWaitingStatus() throws Exception {
        testRendezVous.setStatut(Statut.WAITING);

        byte[] pdfBytes = pdfService.generateAppointmentPdf(testRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGenerateAppointmentPdfWithLongNotes() throws Exception {
        testRendezVous.setNotes("This is a very long note that contains multiple lines of text to test the PDF generation with longer content.");

        byte[] pdfBytes = pdfService.generateAppointmentPdf(testRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGenerateAppointmentPdfWithEmptyNotes() throws Exception {
        testRendezVous.setNotes("");

        byte[] pdfBytes = pdfService.generateAppointmentPdf(testRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGenerateAppointmentPdfWithNullNotes() throws Exception {
        testRendezVous.setNotes(null);

        byte[] pdfBytes = pdfService.generateAppointmentPdf(testRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGenerateAppointmentPdfWithDifferentTime() throws Exception {
        testRendezVous.setHeure(LocalTime.of(14, 45));

        byte[] pdfBytes = pdfService.generateAppointmentPdf(testRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGenerateAppointmentPdfWithDifferentDate() throws Exception {
        testRendezVous.setDate(LocalDate.of(2026, Month.DECEMBER, 25));

        byte[] pdfBytes = pdfService.generateAppointmentPdf(testRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGenerateAppointmentPdfWithSpecialCharactersInName() throws Exception {
        testRendezVous.getUser().setName("Jean-François O'Connor");

        byte[] pdfBytes = pdfService.generateAppointmentPdf(testRendezVous);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
