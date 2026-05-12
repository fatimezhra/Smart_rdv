package com.example.demo.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.example.demo.entities.BlockedDate;
import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.entities.TimeSlot;
import com.example.demo.entities.User;
import com.example.demo.entities.WaitingList;
import com.example.demo.repositories.BlockedDateRepository;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.TimeSlotRepository;
import com.example.demo.repositories.WaitingListRepository;

@Service
public class ReservationService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Autowired
    private BlockedDateRepository blockedDateRepository;

    @Autowired
    private SlotGenerationService slotGenerationService;

    // ===================== RÉSERVER =====================
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseEntity<?> reserver(Long timeSlotId, User user) {

        TimeSlot slot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("TimeSlot introuvable"));

        if (blockedDateRepository.existsByDate(slot.getDate())) {
            throw new RuntimeException("This date is unavailable.");
        }

        // Fix 4: Check if user already has a confirmed appointment on this date
        boolean dejaReserve = rendezVousRepository
                .existsByUserAndDateAndStatut(user, slot.getDate(), Statut.CONFIRMED);
        if (dejaReserve) {
            throw new RuntimeException("Vous avez déjà un rendez-vous confirmé ce jour.");
        }

        if (slot.isDisponible()) {

            slot.setDisponible(false);

            RendezVous rdv = new RendezVous();
            rdv.setDate(slot.getDate());
            rdv.setHeure(slot.getHeure());
            rdv.setStatut(Statut.CONFIRMED);
            rdv.setUser(user);
            rdv.setTimeSlot(slot);
            rdv.setUpdatedAt(LocalDateTime.now());

            timeSlotRepository.save(slot);
            rendezVousRepository.save(rdv);

            // Fix 3: Return proper Map format for CONFIRMED case
            Map<String, Object> result = new HashMap<>();
            result.put("type", "CONFIRMED");
            result.put("rendezVous", rdv);
            return ResponseEntity.ok(result);
        }

        // CAS 2 : alternatives
        List<TimeSlot> alternatives =
                timeSlotRepository.findByDateAndDisponibleTrue(slot.getDate())
                        .stream()
                        .limit(3)
                        .toList();

        if (alternatives.isEmpty()) {

            WaitingList w = new WaitingList();
            w.setDate(slot.getDate());
            w.setUser(user);

            int position = waitingListRepository
                    .findByDateOrderByPositionAsc(slot.getDate())
                    .size() + 1;

            w.setPosition(position);
            waitingListRepository.save(w);

            // Fix 3: Return proper Map format for WAITING_LIST case
            Map<String, Object> result = new HashMap<>();
            result.put("type", "WAITING_LIST");
            result.put("position", w.getPosition());
            return ResponseEntity.ok(result);
        }

        // Fix 3: Return proper Map format for ALTERNATIVES case
        Map<String, Object> result = new HashMap<>();
        result.put("type", "ALTERNATIVES");
        result.put("alternatives", alternatives);
        return ResponseEntity.ok(result);
    }

    // Fix 1: Private method for direct booking without alternatives logic
    @Transactional
    private RendezVous reserverDirectement(TimeSlot slot, User user) {
        slot.setDisponible(false);
        timeSlotRepository.save(slot);
        
        RendezVous rdv = new RendezVous();
        rdv.setUser(user);
        rdv.setTimeSlot(slot);
        rdv.setDate(slot.getDate());
        rdv.setHeure(slot.getHeure());
        rdv.setStatut(Statut.CONFIRMED);
        rdv.setUpdatedAt(LocalDateTime.now());
        
        return rendezVousRepository.save(rdv);
    }

    // ===================== ANNULER =====================
   @Transactional
   public void annuler(Long rdvId) {
    RendezVous rdv = rendezVousRepository.findById(rdvId)
            .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable"));

    TimeSlot slot = rdv.getTimeSlot();
    LocalDate dateRdv = slot.getDate();

    slot.setDisponible(true);
    rdv.setStatut(Statut.CANCELLED);
    rdv.setUpdatedAt(LocalDateTime.now());

    timeSlotRepository.save(slot);
    rendezVousRepository.save(rdv);

    // 1. Récupérer la liste d'attente pour cette date
    List<WaitingList> waitingList = waitingListRepository.findByDateOrderByPositionAsc(dateRdv);

    if (!waitingList.isEmpty()) {
        // 2. Promouvoir le premier
        WaitingList first = waitingList.get(0);
        reserverDirectement(slot, first.getUser());
        waitingListRepository.delete(first);

        // 3. MISE À JOUR CRITIQUE : Décaler tout le monde vers le haut
        // On récupère la liste mise à jour (sans le premier)
        List<WaitingList> remaining = waitingListRepository.findByDateOrderByPositionAsc(dateRdv);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i + 1); // La nouvelle position commence à 1
        }
        waitingListRepository.saveAll(remaining);
    }
}

    // ===================== RESCHEDULE =====================
    @Transactional
    public RendezVous reschedule(Long rdvId, Long newSlotId, User user) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable"));

        if (!rdv.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }

        TimeSlot newSlot = timeSlotRepository.findById(newSlotId)
                .orElseThrow(() -> new RuntimeException("TimeSlot introuvable"));

        if (!newSlot.isDisponible()) {
            throw new RuntimeException("New slot is not available");
        }

        if (blockedDateRepository.existsByDate(newSlot.getDate())) {
            throw new RuntimeException("This date is unavailable.");
        }

        // Free old slot
        TimeSlot oldSlot = rdv.getTimeSlot();
        oldSlot.setDisponible(true);
        timeSlotRepository.save(oldSlot);

        // Book new slot
        newSlot.setDisponible(false);
        timeSlotRepository.save(newSlot);

        rdv.setTimeSlot(newSlot);
        rdv.setDate(newSlot.getDate());
        rdv.setHeure(newSlot.getHeure());
        rdv.setUpdatedAt(LocalDateTime.now());

        // Promote waiting list for old slot date
        List<WaitingList> waitingList = waitingListRepository.findByDateOrderByPositionAsc(oldSlot.getDate());
        if (!waitingList.isEmpty()) {
            WaitingList first = waitingList.get(0);
            reserverDirectement(oldSlot, first.getUser());
            waitingListRepository.delete(first);
            List<WaitingList> remaining = waitingListRepository.findByDateOrderByPositionAsc(oldSlot.getDate());
            for (int i = 0; i < remaining.size(); i++) {
                remaining.get(i).setPosition(i + 1);
            }
            waitingListRepository.saveAll(remaining);
        }

        return rendezVousRepository.save(rdv);
    }

    // ===================== NOTES =====================
    public RendezVous addNotes(Long rdvId, String notes, User user) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable"));
        if (!rdv.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }
        rdv.setNotes(notes);
        rdv.setUpdatedAt(LocalDateTime.now());
        return rendezVousRepository.save(rdv);
    }

    // ===================== GET METHODS =====================
    public List<RendezVous> getMyReservations() {
        User currentUser = getCurrentUser();
        return rendezVousRepository.findByUser(currentUser);
    }

    public List<RendezVous> getUpcoming() {
        User currentUser = getCurrentUser();
        return rendezVousRepository.findByUserAndStatutInOrderByDateDesc(
            currentUser, 
            List.of(Statut.CONFIRMED, Statut.WAITING)
        );
    }

    private User getCurrentUser() {
        // This would typically be injected or retrieved from security context
        // For now, return a placeholder or throw exception
        throw new RuntimeException("User context not available in service layer");
    }
}