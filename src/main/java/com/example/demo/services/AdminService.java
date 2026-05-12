package com.example.demo.services;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entities.BlockedDate;
import com.example.demo.entities.Role;
import com.example.demo.entities.Statut;
import com.example.demo.repositories.BlockedDateRepository;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.WaitingListRepository;

@Service
public class AdminService {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlockedDateRepository blockedDateRepository;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.now();

        data.put("totalUsers", userRepository.count());
        data.put("totalReservations", rendezVousRepository.count());
        data.put("confirmedToday", rendezVousRepository.countByStatutAndDate(Statut.CONFIRMED, today));
        data.put("cancelledToday", rendezVousRepository.countByStatutAndDate(Statut.CANCELLED, today));
        data.put("waitingListCount", waitingListRepository.count());
        data.put("upcomingAppointments", rendezVousRepository.findByStatutAndDate(Statut.CONFIRMED, today));
        data.put("recentCancellations", rendezVousRepository.findTop10ByOrderByUpdatedAtDesc().stream()
                .filter(r -> r.getStatut() == Statut.CANCELLED)
                .limit(10)
                .toList());
        data.put("blockedDatesThisMonth", blockedDateRepository.findByDateBetween(
                thisMonth.atDay(1), thisMonth.atEndOfMonth()));
        return data;
    }
}