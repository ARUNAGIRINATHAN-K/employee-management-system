package com.ems.employee_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Data Transfer Object representing an employee's leave or WFH request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private String approvedByUsername;
}
