package com.example.demo.dto;

import java.time.LocalDate;

public class BlockedDateDTO {
    private LocalDate date;
    private String reason;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
