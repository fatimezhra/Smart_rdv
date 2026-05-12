package com.example.demo.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.entities.User;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    List<RendezVous> findByDate(LocalDate date);
    List<RendezVous> findByUser(User user);
    List<RendezVous> findByUserAndStatut(User user, Statut statut);
    List<RendezVous> findByUserAndStatutInOrderByDateDesc(User user, List<Statut> statuts);
    Page<RendezVous> findByDateAndStatut(LocalDate date, Statut statut, Pageable pageable);
    Page<RendezVous> findByDate(LocalDate date, Pageable pageable);
    Page<RendezVous> findByStatut(Statut statut, Pageable pageable);
    Page<RendezVous> findByUser(User user, Pageable pageable);
    Page<RendezVous> findAll(Pageable pageable);
    long countByStatutAndDate(Statut statut, LocalDate date);
    long countByUser(User user);
    List<RendezVous> findTop10ByOrderByUpdatedAtDesc();
    List<RendezVous> findByStatutAndDate(Statut statut, LocalDate date);
    boolean existsByUserAndDateAndStatut(User user, LocalDate date, Statut statut);
}