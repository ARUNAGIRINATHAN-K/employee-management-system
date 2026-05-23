package com.ems.service;

import com.ems.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Employee counts
        long totalActive = employeeRepository.countActiveEmployees();
        long inactive = employeeRepository.countByStatus("INACTIVE");
        stats.put("totalEmployees", totalActive + inactive);
        stats.put("activeEmployees", totalActive);
        stats.put("inactiveEmployees", inactive);

        // 2. Department counts
        long totalDepartments = departmentRepository.count();
        stats.put("totalDepartments", totalDepartments);

        // Department-wise employee counts
        List<Object[]> deptCounts = employeeRepository.countEmployeesByDepartment();
        Map<String, Long> departmentWise = deptCounts.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0] != null ? (String) arr[0] : "No Department",
                        arr -> (Long) arr[1],
                        (existing, replacement) -> existing
                ));
        stats.put("departmentWiseCounts", departmentWise);

        // 3. Leave Stats
        long pendingLeaves = leaveRepository.findByStatus("PENDING").size();
        stats.put("pendingLeaves", pendingLeaves);

        List<Object[]> leaveTypeCounts = leaveRepository.countLeavesByType();
        Map<String, Long> leaveStats = leaveTypeCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1],
                        (existing, replacement) -> existing
                ));
        stats.put("leaveStats", leaveStats);

        // 4. Recent activity (top 5 audit logs)
        stats.put("recentActivity", auditLogRepository.findAllByOrderByTimestampDesc().stream().limit(6).collect(Collectors.toList()));

        return stats;
    }
}
