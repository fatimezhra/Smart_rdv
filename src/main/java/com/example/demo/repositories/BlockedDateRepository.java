package com.example.demo.repositories;

import com.example.demo.entities.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {
    Optional<BlockedDate> findByDate(LocalDate date);
    List<BlockedDate> findByDateBetween(LocalDate start, LocalDate end);
    boolean existsByDate(LocalDate date);
}
