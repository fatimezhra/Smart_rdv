package com.example.demo.services;

import java.util.List;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.entities.TimeSlot;
import com.example.demo.entities.User;
import com.example.demo.entities.WaitingList;
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

    // ===================== RÉSERVER =====================
    public ResponseEntity<?> reserver(Long timeSlotId, User user) {

        TimeSlot slot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("TimeSlot introuvable"));

        if (slot.isDisponible()) {

            slot.setDisponible(false);

            RendezVous rdv = new RendezVous();
            rdv.setDate(slot.getDate());
            rdv.setHeure(slot.getHeure());
            rdv.setStatut(Statut.CONFIRMED);
            rdv.setUser(user);
            rdv.setTimeSlot(slot);

            timeSlotRepository.save(slot);
            rendezVousRepository.save(rdv);

            return ResponseEntity.ok(rdv);
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

            return ResponseEntity.ok("Ajouté à la liste d'attente");
        }

        return ResponseEntity.ok(alternatives);
    }

    // ===================== ANNULER =====================
   public void annuler(Long rdvId) {
    RendezVous rdv = rendezVousRepository.findById(rdvId)
            .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable"));

    TimeSlot slot = rdv.getTimeSlot();
    LocalDate dateRdv = slot.getDate();
    
    slot.setDisponible(true);
    rdv.setStatut(Statut.CANCELLED);

    timeSlotRepository.save(slot);
    rendezVousRepository.save(rdv);

    // 1. Récupérer la liste d'attente pour cette date
    List<WaitingList> waitingList = waitingListRepository.findByDateOrderByPositionAsc(dateRdv);

    if (!waitingList.isEmpty()) {
        // 2. Promouvoir le premier
        WaitingList first = waitingList.get(0);
        reserver(slot.getId(), first.getUser());
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
    
}