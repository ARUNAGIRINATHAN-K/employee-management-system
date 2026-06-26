package com.ems.employee_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an employee's attendance log.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String status;
    private String workMode;
    private int overtimeMinutes;
    private int lateMinutes;
}
