package com.ems.employee_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object for Dashboard metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalEmployees;
    private long activeEmployees;
    private long totalDepartments;
    private BigDecimal averageSalary;
    private Map<String, Long> employeesPerDepartment;
    
    // Manager-specific department-scoped fields
    private String departmentName;
    private Long departmentId;
    private Map<String, Long> teamDistribution;
}

