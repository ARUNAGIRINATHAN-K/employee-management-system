package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.dto.DashboardStatsDTO;
import com.ems.employee_management_system.model.Department;
import com.ems.employee_management_system.model.Employee;
import com.ems.employee_management_system.model.EmployeeStatus;
import com.ems.employee_management_system.model.User;
import com.ems.employee_management_system.repository.DepartmentRepository;
import com.ems.employee_management_system.repository.EmployeeRepository;
import com.ems.employee_management_system.repository.UserRepository;
import com.ems.employee_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service implementation for aggregating dashboard statistics.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isManager = false;
        String username = null;
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
            isManager = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")) &&
                    auth.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        }

        if (isManager && username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                Optional<Employee> employeeOpt = employeeRepository.findByUserId(userOpt.get().getId());
                if (employeeOpt.isPresent() && employeeOpt.get().getDepartment() != null) {
                    Department dept = employeeOpt.get().getDepartment();
                    Long deptId = dept.getId();
                    String deptName = dept.getName();

                    long totalEmployees = employeeRepository.countByDepartmentId(deptId);
                    long activeEmployees = employeeRepository.countByDepartmentIdAndStatus(deptId, EmployeeStatus.ACTIVE);

                    BigDecimal averageSalary = employeeRepository.getAverageSalaryOfActiveEmployeesByDepartment(deptId);
                    if (averageSalary == null) {
                        averageSalary = BigDecimal.ZERO;
                    } else {
                        averageSalary = averageSalary.setScale(2, RoundingMode.HALF_UP);
                    }

                    List<Object[]> jobCounts = employeeRepository.getActiveEmployeeCountByJobTitleForDepartment(deptId);
                    Map<String, Long> teamDistribution = new HashMap<>();
                    for (Object[] row : jobCounts) {
                        String jobTitle = (String) row[0];
                        Long count = (Long) row[1];
                        if (jobTitle != null) {
                            teamDistribution.put(jobTitle, count);
                        }
                    }

                    return DashboardStatsDTO.builder()
                            .totalEmployees(totalEmployees)
                            .activeEmployees(activeEmployees)
                            .totalDepartments(1)
                            .averageSalary(averageSalary)
                            .departmentId(deptId)
                            .departmentName(deptName)
                            .employeesPerDepartment(new HashMap<>())
                            .teamDistribution(teamDistribution)
                            .build();
                }
            }
        }

        // Default global stats for ADMIN/HR
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

