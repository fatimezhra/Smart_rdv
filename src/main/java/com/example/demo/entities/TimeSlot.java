package com.example.demo.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "time_slot", indexes = {
    @Index(name = "idx_slot_date", columnList = "date"),
    @Index(name = "idx_slot_disponible", columnList = "disponible")
})
@Getter
@Setter
public class TimeSlot {

    @Id
    @GeneratedValue
    private Long id;
    private LocalDate date;
    private LocalTime heure;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean disponible = true;

    // Essential getters for Lombok compatibility
    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}