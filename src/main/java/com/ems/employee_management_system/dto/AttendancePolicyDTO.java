package com.ems.employee_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

/**
 * Data Transfer Object for AttendancePolicy configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicyDTO {
    private Long id;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private int gracePeriodMinutes;
    private int overtimeThresholdMinutes;
}
