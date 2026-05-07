package com.example.demo.repositories;

import com.example.demo.entities.WaitingList;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {
    List<WaitingList> findByDateOrderByPositionAsc(LocalDate date);
}