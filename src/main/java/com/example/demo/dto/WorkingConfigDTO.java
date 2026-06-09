package com.example.demo.dto;

import com.example.demo.entities.WorkingConfig;

public class WorkingConfigDTO extends BaseWorkingConfig {

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
