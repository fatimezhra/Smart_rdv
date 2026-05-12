package com.example.demo.controllers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.AdminService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminService.getDashboardData();
    }

    @GetMapping("/reservations")
    public Page<RendezVous> getReservations(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (date != null && status != null) {
            LocalDate d = LocalDate.parse(date);
            Statut s = Statut.valueOf(status);
            return rendezVousRepository.findByDateAndStatut(d, s, pageable);
        } else if (date != null) {
            LocalDate d = LocalDate.parse(date);
            return rendezVousRepository.findByDate(d, pageable);
        } else if (status != null) {
            Statut s = Statut.valueOf(status);
            return rendezVousRepository.findByStatut(s, pageable);
        } else if (userId != null) {
            return rendezVousRepository.findByUser(
                    userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")),
                    pageable);
        }
        return rendezVousRepository.findAll(pageable);
    }
}