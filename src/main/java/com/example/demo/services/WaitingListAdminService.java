package com.example.demo.services;

import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.entities.TimeSlot;
import com.example.demo.entities.WaitingList;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.TimeSlotRepository;
import com.example.demo.repositories.WaitingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class WaitingListAdminService {

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private ReservationService reservationService;

    public List<WaitingList> getFullWaitingList() {
        return waitingListRepository.findAllByOrderByDateAscPositionAsc();
    }

    @Transactional
    public void promoteWaitingListEntry(Long id) {
        WaitingList entry = waitingListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Waiting list entry not found"));

        LocalDate date = entry.getDate();
        List<TimeSlot> availableSlots = timeSlotRepository.findByDateAndDisponibleTrue(date);

        if (availableSlots.isEmpty()) {
            throw new RuntimeException("No available slots for this date");
        }

        TimeSlot slot = availableSlots.get(0);
        reservationService.reserver(slot.getId(), entry.getUser());
        waitingListRepository.delete(entry);

        // Recalculate positions
        List<WaitingList> remaining = waitingListRepository.findByDateOrderByPositionAsc(date);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i + 1);
        }
        waitingListRepository.saveAll(remaining);
    }

    @Transactional
    public void removeFromWaitingList(Long id) {
        WaitingList entry = waitingListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Waiting list entry not found"));

        LocalDate date = entry.getDate();
        waitingListRepository.delete(entry);

        // Recalculate positions
        List<WaitingList> remaining = waitingListRepository.findByDateOrderByPositionAsc(date);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i + 1);
        }
        waitingListRepository.saveAll(remaining);
    }
}
