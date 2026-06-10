package com.example.demo.services;

import com.example.demo.entities.RendezVous;
import com.example.demo.entities.User;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IReservationService {
    ResponseEntity<Map<String, Object>> reserver(Long timeSlotId, User user);
    void annuler(Long rdvId);
    RendezVous reschedule(Long rdvId, Long newSlotId, User user);
    RendezVous addNotes(Long rdvId, String notes, User user);
    Map<String, Object> joinWaitingList(LocalDate date, User user);
    List<RendezVous> getMyReservations();
    List<RendezVous> getUpcoming();
}
