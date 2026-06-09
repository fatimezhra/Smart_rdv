package com.example.demo.dto;

import com.example.demo.entities.WorkingConfig;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkingConfigDTO(
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    int slotDurationMinutes
) {
    public static WorkingConfigDTO fromEntity(WorkingConfig entity) {
        return new WorkingConfigDTO(
            entity.getDayOfWeek(),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.getSlotDurationMinutes()
        );
    }

    public WorkingConfig toEntity() {
        WorkingConfig config = new WorkingConfig();
        config.setDayOfWeek(this.dayOfWeek());
        config.setStartTime(this.startTime());
        config.setEndTime(this.endTime());
        config.setSlotDurationMinutes(this.slotDurationMinutes());
        return config;
    }
}
