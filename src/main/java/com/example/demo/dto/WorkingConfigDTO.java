package com.example.demo.dto;

import com.example.demo.entities.WorkingConfig;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class WorkingConfigDTO {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotDurationMinutes;

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(int slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public static WorkingConfigDTO fromEntity(WorkingConfig entity) {
        WorkingConfigDTO dto = new WorkingConfigDTO();
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setSlotDurationMinutes(entity.getSlotDurationMinutes());
        return dto;
    }

    public WorkingConfig toEntity() {
        WorkingConfig config = new WorkingConfig();
        config.setDayOfWeek(this.dayOfWeek);
        config.setStartTime(this.startTime);
        config.setEndTime(this.endTime);
        config.setSlotDurationMinutes(this.slotDurationMinutes);
        return config;
    }
}
