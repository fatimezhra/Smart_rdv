package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.TimeSlot;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

	List<TimeSlot> findByDateAndDisponibleTrue(LocalDate date);
	List<TimeSlot> findByDate(LocalDate date);
	long countByDateAndDisponibleTrue(LocalDate date);
	long countByDate(LocalDate date);
}