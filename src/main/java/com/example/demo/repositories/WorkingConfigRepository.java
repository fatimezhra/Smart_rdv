package com.example.demo.repositories;

import com.example.demo.entities.WorkingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.Optional;

@Repository
public interface WorkingConfigRepository extends JpaRepository<WorkingConfig, Long> {
    Optional<WorkingConfig> findByDayOfWeek(DayOfWeek dayOfWeek);
}
