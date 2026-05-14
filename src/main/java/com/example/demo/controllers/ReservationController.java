package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; // Utilisation de * pour simplifier les imports

import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.entities.User;
import com.example.demo.entities.WaitingList;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.WaitingListRepository;
import com.example.demo.services.PdfService;
import com.example.demo.services.ReservationService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
// --- AJOUTER CETTE LIGNE CI-DESSOUS ---
@CrossOrigin(origins = "http://localhost:3001", allowedHeaders = "*", allowCredentials = "true")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Autowired
    private PdfService pdfService;

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Not authenticated");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<?> getMyReservations() {
        User user = getCurrentUser();
        return rendezVousRepository.findByUser(user);
    }

    @GetMapping("/upcoming")
    public List<RendezVous> getUpcoming() {
        User user = getCurrentUser();
        return rendezVousRepository.findByUserAndStatut(user, Statut.CONFIRMED);
    }

    @GetMapping("/history")
    public List<RendezVous> getHistory() {
        User user = getCurrentUser();
        return rendezVousRepository.findByUserAndStatutInOrderByDateDesc(
                user, Arrays.asList(Statut.CANCELLED));
    }

    @PostMapping("/{slotId}")
    public Object reserver(@PathVariable Long slotId) {
        User user = getCurrentUser();
        return reservationService.reserver(slotId, user);
    }

    @DeleteMapping("/{id}")
    public void annuler(@PathVariable Long id) {
        reservationService.annuler(id);
    }

    @PutMapping("/{id}/reschedule")
    public RendezVous reschedule(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        User user = getCurrentUser();
        Long newSlotId = body.get("newSlotId");
        return reservationService.reschedule(id, newSlotId, user);
    }

    @PutMapping("/{id}/notes")
    public RendezVous addNotes(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = getCurrentUser();
        return reservationService.addNotes(id, body.get("notes"), user);
    }

    @GetMapping("/waiting")
    public List<WaitingList> getWaitingList() {
        User user = getCurrentUser();
        return waitingListRepository.findByUserOrderByPositionAsc(user);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getAppointmentPdf(@PathVariable Long id) {
        User user = getCurrentUser();
        
        RendezVous rendezVous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (!rendezVous.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: This appointment does not belong to you");
        }
        
        try {
            byte[] pdfContent = pdfService.generateAppointmentPdf(rendezVous);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "appointment_" + rendezVous.getId() + "_" + rendezVous.getDate() + ".pdf");
            headers.setContentLength(pdfContent.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
                    
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}
