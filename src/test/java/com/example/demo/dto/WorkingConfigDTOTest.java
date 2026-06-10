package com.example.demo.dto;

import com.example.demo.entities.WorkingConfig;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkingConfigDTOTest {

    @Test
    void fromEntity_ShouldConvertEntityToDTO() {
        // Given
        WorkingConfig entity = new WorkingConfig();
        entity.setId(1L);
        entity.setDayOfWeek(DayOfWeek.MONDAY);
        entity.setStartTime(LocalTime.of(9, 0));
        entity.setEndTime(LocalTime.of(17, 0));
        entity.setSlotDurationMinutes(30);

        // When
        WorkingConfigDTO dto = WorkingConfigDTO.fromEntity(entity);

        // Then
        assertNotNull(dto);
        assertEquals(DayOfWeek.MONDAY, dto.dayOfWeek());
        assertEquals(LocalTime.of(9, 0), dto.startTime());
        assertEquals(LocalTime.of(17, 0), dto.endTime());
        assertEquals(30, dto.slotDurationMinutes());
    }

    @Test
    void fromEntity_ShouldConvertEntityWithDifferentValues() {
        // Given
        WorkingConfig entity = new WorkingConfig();
        entity.setId(2L);
        entity.setDayOfWeek(DayOfWeek.TUESDAY);
        entity.setStartTime(LocalTime.of(8, 30));
        entity.setEndTime(LocalTime.of(16, 30));
        entity.setSlotDurationMinutes(45);

        // When
        WorkingConfigDTO dto = WorkingConfigDTO.fromEntity(entity);

        // Then
        assertNotNull(dto);
        assertEquals(DayOfWeek.TUESDAY, dto.dayOfWeek());
        assertEquals(LocalTime.of(8, 30), dto.startTime());
        assertEquals(LocalTime.of(16, 30), dto.endTime());
        assertEquals(45, dto.slotDurationMinutes());
    }

    @Test
    void toEntity_ShouldConvertDTOToEntity() {
        // Given
        WorkingConfigDTO dto = new WorkingConfigDTO(
            DayOfWeek.WEDNESDAY,
            LocalTime.of(10, 0),
            LocalTime.of(18, 0),
            60
        );

        // When
        WorkingConfig entity = dto.toEntity();

        // Then
        assertNotNull(entity);
        assertEquals(DayOfWeek.WEDNESDAY, entity.getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), entity.getStartTime());
        assertEquals(LocalTime.of(18, 0), entity.getEndTime());
        assertEquals(60, entity.getSlotDurationMinutes());
    }

    @Test
    void toEntity_ShouldConvertDTOWithDifferentValues() {
        // Given
        WorkingConfigDTO dto = new WorkingConfigDTO(
            DayOfWeek.FRIDAY,
            LocalTime.of(7, 0),
            LocalTime.of(15, 0),
            15
        );

        // When
        WorkingConfig entity = dto.toEntity();

        // Then
        assertNotNull(entity);
        assertEquals(DayOfWeek.FRIDAY, entity.getDayOfWeek());
        assertEquals(LocalTime.of(7, 0), entity.getStartTime());
        assertEquals(LocalTime.of(15, 0), entity.getEndTime());
        assertEquals(15, entity.getSlotDurationMinutes());
    }

    @Test
    void fromEntity_ShouldHandleNullEntity() {
        // Given
        WorkingConfig entity = new WorkingConfig();
        entity.setDayOfWeek(null);
        entity.setStartTime(null);
        entity.setEndTime(null);
        entity.setSlotDurationMinutes(0);

        // When
        WorkingConfigDTO dto = WorkingConfigDTO.fromEntity(entity);

        // Then
        assertNotNull(dto);
        assertNull(dto.dayOfWeek());
        assertNull(dto.startTime());
        assertNull(dto.endTime());
        assertEquals(0, dto.slotDurationMinutes());
    }

    @Test
    void toEntity_ShouldHandleNullDTOValues() {
        // Given
        WorkingConfigDTO dto = new WorkingConfigDTO(null, null, null, 0);

        // When
        WorkingConfig entity = dto.toEntity();

        // Then
        assertNotNull(entity);
        assertNull(entity.getDayOfWeek());
        assertNull(entity.getStartTime());
        assertNull(entity.getEndTime());
        assertEquals(0, entity.getSlotDurationMinutes());
    }

    @Test
    void roundTrip_ShouldPreserveData() {
        // Given
        WorkingConfig originalEntity = new WorkingConfig();
        originalEntity.setId(1L);
        originalEntity.setDayOfWeek(DayOfWeek.THURSDAY);
        originalEntity.setStartTime(LocalTime.of(11, 0));
        originalEntity.setEndTime(LocalTime.of(19, 0));
        originalEntity.setSlotDurationMinutes(20);

        // When
        WorkingConfigDTO dto = WorkingConfigDTO.fromEntity(originalEntity);
        WorkingConfig convertedEntity = dto.toEntity();

        // Then
        assertEquals(originalEntity.getDayOfWeek(), convertedEntity.getDayOfWeek());
        assertEquals(originalEntity.getStartTime(), convertedEntity.getStartTime());
        assertEquals(originalEntity.getEndTime(), convertedEntity.getEndTime());
        assertEquals(originalEntity.getSlotDurationMinutes(), convertedEntity.getSlotDurationMinutes());
    }

    @Test
    void recordComponents_ShouldBeAccessible() {
        // Given
        WorkingConfigDTO dto = new WorkingConfigDTO(
            DayOfWeek.SATURDAY,
            LocalTime.of(12, 0),
            LocalTime.of(20, 0),
            90
        );

        // Then
        assertEquals(DayOfWeek.SATURDAY, dto.dayOfWeek());
        assertEquals(LocalTime.of(12, 0), dto.startTime());
        assertEquals(LocalTime.of(20, 0), dto.endTime());
        assertEquals(90, dto.slotDurationMinutes());
    }
}
