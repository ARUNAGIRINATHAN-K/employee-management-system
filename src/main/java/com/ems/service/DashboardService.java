package com.ems.service;

import com.ems.repository.*;
import com.ems.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ems.entity.User;
import com.ems.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.ems.entity.Leave;

@Service
public class DashboardService {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private ProfileChangeRequestRepository profileChangeRequestRepository;

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

        @Autowired
        private UserRepository userRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Determine caller role and scope
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : null;
            User actingUser = null;
            if (username != null) {
                actingUser = userRepository.findByUsername(username).orElse(null);
            }
            boolean isManager = actingUser != null && "ROLE_MANAGER".equals(actingUser.getRole());

            Long tempDeptId = null;
            Employee actingEmployee = actingUser != null ? actingUser.getEmployee() : null;
            if (isManager && actingEmployee != null && actingEmployee.getDepartment() != null) {
                tempDeptId = actingEmployee.getDepartment().getId();
            }
            final Long scopedDeptId = tempDeptId;

                // 1. Employee counts (scoped for manager)
                if (scopedDeptId != null) {
                        List<com.ems.entity.Employee> deptEmployees = employeeRepository.findByDepartmentIdAndStatusNot(scopedDeptId, "DELETED");
                        long totalEmp = deptEmployees.size();
                        long activeEmp = deptEmployees.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus())).count();
                        long inactiveEmp = deptEmployees.stream().filter(e -> "INACTIVE".equalsIgnoreCase(e.getStatus())).count();
                        stats.put("totalEmployees", totalEmp);
                        stats.put("activeEmployees", activeEmp);
                        stats.put("inactiveEmployees", inactiveEmp);
                } else {
                        long totalActive = employeeRepository.countActiveEmployees();
                        long inactive = employeeRepository.countByStatus("INACTIVE");
                        stats.put("totalEmployees", totalActive + inactive);
                        stats.put("activeEmployees", totalActive);
                        stats.put("inactiveEmployees", inactive);
                }

                // 2. Department counts (scoped for manager -> single department)
                if (scopedDeptId != null) {
                        stats.put("totalDepartments", 1);
                } else {
                        long totalDepartments = departmentRepository.count();
                        stats.put("totalDepartments", totalDepartments);
                }

        // Department-wise employee counts (scoped)
        if (scopedDeptId != null) {
            String deptName = departmentRepository.findById(scopedDeptId).map(d -> d.getName()).orElse("Department");
            Map<String, Long> departmentWise = new HashMap<>();
            departmentWise.put(deptName, (Long) (long) employeeRepository.findByDepartmentIdAndStatusNot(scopedDeptId, "DELETED").size());
            stats.put("departmentWiseCounts", departmentWise);
        } else {
            List<Object[]> deptCounts = employeeRepository.countEmployeesByDepartment();
            Map<String, Long> departmentWise = deptCounts.stream()
                    .collect(Collectors.toMap(
                            arr -> arr[0] != null ? (String) arr[0] : "No Department",
                            arr -> (Long) arr[1],
                            (existing, replacement) -> existing
                    ));
            stats.put("departmentWiseCounts", departmentWise);
        }

        // 3. Leave Stats
                // 3. Leave Stats (scoped)
                long pendingLeaves;
                if (scopedDeptId != null) {
                        pendingLeaves = leaveRepository.findByEmployeeDepartmentId(scopedDeptId).stream()
                                        .filter(l -> "PENDING".equalsIgnoreCase(l.getStatus())).count();
                } else {
                        pendingLeaves = leaveRepository.findByStatus("PENDING").size();
                }
                stats.put("pendingLeaves", pendingLeaves);

        Map<String, Long> leaveStats = new HashMap<>();
        if (scopedDeptId != null) {
            List<Leave> deptLeaves = leaveRepository.findByEmployeeDepartmentId(scopedDeptId);
            for (Leave l : deptLeaves) {
                String lt = l.getLeaveType() != null ? l.getLeaveType() : "OTHER";
                leaveStats.merge(lt, 1L, Long::sum);
            }
        } else {
            List<Object[]> leaveTypeCounts = leaveRepository.countLeavesByType();
            leaveStats = leaveTypeCounts.stream()
                    .collect(Collectors.toMap(
                            arr -> (String) arr[0],
                            arr -> (Long) arr[1],
                            (existing, replacement) -> existing
                    ));
        }
        stats.put("leaveStats", leaveStats);

                // 4. Recent activity (top 6 audit logs) - scoped for manager to department events
                List<com.ems.entity.AuditLog> recent = auditLogRepository.findAllByOrderByTimestampDesc();
                if (scopedDeptId != null) {
                        // collect department usernames and full names
                        List<com.ems.entity.Employee> deptEmployees = employeeRepository.findByDepartmentIdAndStatusNot(scopedDeptId, "DELETED");
                        java.util.Set<String> deptUsernames = userRepository.findAll().stream()
                                        .filter(u -> u.getEmployee() != null && u.getEmployee().getDepartment() != null && scopedDeptId.equals(u.getEmployee().getDepartment().getId()))
                                        .map(u -> u.getUsername())
                                        .collect(Collectors.toSet());
                        java.util.Set<String> deptNames = deptEmployees.stream()
                                        .map(e -> (e.getFirstName() != null ? e.getFirstName() : "") + " " + (e.getLastName() != null ? e.getLastName() : ""))
                                        .collect(Collectors.toSet());

                        List<com.ems.entity.AuditLog> filtered = recent.stream().filter(a -> {
                                if (a == null) return false;
                                if (a.getUsername() != null && deptUsernames.contains(a.getUsername())) return true;
                                if (a.getDetails() != null) {
                                        for (String n : deptNames) {
                                                if (n != null && !n.trim().isEmpty() && a.getDetails().contains(n.trim())) return true;
                                        }
                                }
                                return false;
                        }).limit(6).collect(Collectors.toList());
                        stats.put("recentActivity", filtered);
                } else {
                        stats.put("recentActivity", recent.stream().limit(6).collect(Collectors.toList()));
                }

                // 5. Pending profile changes (scoped)
                long pendingProfileRequests;
                if (scopedDeptId != null) {
                        pendingProfileRequests = profileChangeRequestRepository.findByEmployeeDepartmentId(scopedDeptId).stream()
                                        .filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).count();
                } else {
                        pendingProfileRequests = profileChangeRequestRepository.findByStatus("PENDING").size();
                }
                stats.put("pendingProfileRequests", pendingProfileRequests);

                // 6. Pending expense claims (scoped)
                long pendingExpenseClaims;
                if (scopedDeptId != null) {
                        pendingExpenseClaims = expenseClaimRepository.findByEmployeeDepartmentId(scopedDeptId).stream()
                                        .filter(ec -> "PENDING".equalsIgnoreCase(ec.getStatus())).count();
                } else {
                        pendingExpenseClaims = expenseClaimRepository.findByStatus("PENDING").size();
                }
                stats.put("pendingExpenseClaims", pendingExpenseClaims);

                String currentPeriod = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
                double totalPayroll = 0.0;
                if (scopedDeptId != null) {
                        List<com.ems.entity.Employee> deptEmployees = employeeRepository.findByDepartmentIdAndStatusNot(scopedDeptId, "DELETED");
                        double sum = 0.0;
                        for (com.ems.entity.Employee e : deptEmployees) {
                                java.util.Optional<com.ems.entity.Payroll> p = payrollRepository.findByEmployeeIdAndPayPeriod(e.getId(), currentPeriod);
                                if (p.isPresent()) {
                                        sum += p.get().getNetSalary();
                                }
                        }
                        totalPayroll = sum;
                } else {
                        List<com.ems.entity.Payroll> currentPayroll = payrollRepository.findByPayPeriod(currentPeriod);
                        totalPayroll = currentPayroll.stream().mapToDouble(com.ems.entity.Payroll::getNetSalary).sum();
                }
                stats.put("totalMonthlyPayroll", Math.round(totalPayroll * 100.0) / 100.0);

                        return stats;
                } catch (Exception ex) {
                        log.error("Error computing dashboard stats", ex);
                        // Return safe defaults instead of throwing NPEs that cause 500
                        stats.putIfAbsent("totalEmployees", 0L);
                        stats.putIfAbsent("activeEmployees", 0L);
                        stats.putIfAbsent("inactiveEmployees", 0L);
                        stats.putIfAbsent("totalDepartments", 0L);
                        stats.putIfAbsent("departmentWiseCounts", Map.of());
                        stats.putIfAbsent("pendingLeaves", 0L);
                        stats.putIfAbsent("leaveStats", Map.of());
                        stats.putIfAbsent("recentActivity", List.of());
                        stats.putIfAbsent("pendingProfileRequests", 0L);
                        stats.putIfAbsent("pendingExpenseClaims", 0L);
                        stats.putIfAbsent("totalMonthlyPayroll", 0.0);
                        stats.put("error", "Failed to compute dashboard stats - see server logs");
                        return stats;
                }
    }
}
