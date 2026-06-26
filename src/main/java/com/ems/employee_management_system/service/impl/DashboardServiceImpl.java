package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.dto.DashboardStatsDTO;
import com.ems.employee_management_system.repository.DepartmentRepository;
import com.ems.employee_management_system.repository.EmployeeRepository;
import com.ems.employee_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for aggregating dashboard statistics.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatusActive();
        long totalDepartments = departmentRepository.count();

        BigDecimal averageSalary = employeeRepository.getAverageSalaryOfActiveEmployees();
        if (averageSalary == null) {
            averageSalary = BigDecimal.ZERO;
        } else {
            averageSalary = averageSalary.setScale(2, RoundingMode.HALF_UP);
        }

        List<Object[]> deptCounts = employeeRepository.getActiveEmployeeCountByDepartment();
        Map<String, Long> employeesPerDepartment = new HashMap<>();
        for (Object[] row : deptCounts) {
            String deptName = (String) row[0];
            Long count = (Long) row[1];
            if (deptName != null) {
                employeesPerDepartment.put(deptName, count);
            }
        }

        return DashboardStatsDTO.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .totalDepartments(totalDepartments)
                .averageSalary(averageSalary)
                .employeesPerDepartment(employeesPerDepartment)
                .build();
    }
}
