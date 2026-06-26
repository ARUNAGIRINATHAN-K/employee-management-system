package com.ems.employee_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object containing month-level aggregated attendance stats for employee profiles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryDTO {
    private int presentDays;
    private int lateDays;
    private int absentDays;
    private int leaveDays;
    private int wfhDays;
    private int weekendDays;
    private int holidayDays;
    private int totalOvertimeMinutes;
    private int totalLateMinutes;
}
