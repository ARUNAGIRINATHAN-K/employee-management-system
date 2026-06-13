package com.ems.controller;

import com.ems.entity.Leave;
import com.ems.entity.LeaveBalance;
import com.ems.entity.LeavePolicy;
import com.ems.service.LeaveService;
import com.ems.repository.EmployeeRepository;
import com.ems.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<List<Leave>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Leave>> getEmployeeLeaves(@PathVariable Long employeeId) {
        // Allow HR to view any; Managers only for their department; Employees only their own
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            UserPrincipal up = (UserPrincipal) principal;
            String role = up.getRole();
            if ("ROLE_EMPLOYEE".equals(role)) {
                Long myEmpId = up.getUser() != null && up.getUser().getEmployee() != null ? up.getUser().getEmployee().getId() : null;
                if (myEmpId == null || !myEmpId.equals(employeeId)) {
                    return ResponseEntity.status(403).build();
                }
            } else if ("ROLE_MANAGER".equals(role)) {
                Long deptId = up.getDepartmentId();
                if (deptId == null) return ResponseEntity.status(403).build();
                var empOpt = employeeRepository.findById(employeeId);
                if (empOpt.isEmpty() || empOpt.get().getDepartment() == null || !deptId.equals(empOpt.get().getDepartment().getId())) {
                    return ResponseEntity.status(403).build();
                }
            }
        }
        return ResponseEntity.ok(leaveService.getLeavesByEmployee(employeeId));
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<List<Leave>> getManagerLeaves(@PathVariable Long managerId) {
        return ResponseEntity.ok(leaveService.getLeavesForManager(managerId));
    }

    @GetMapping("/balances/employee/{employeeId}")
    public ResponseEntity<List<LeaveBalance>> getEmployeeLeaveBalances(@PathVariable Long employeeId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            UserPrincipal up = (UserPrincipal) principal;
            String role = up.getRole();
            if ("ROLE_EMPLOYEE".equals(role)) {
                Long myEmpId = up.getUser() != null && up.getUser().getEmployee() != null ? up.getUser().getEmployee().getId() : null;
                if (myEmpId == null || !myEmpId.equals(employeeId)) {
                    return ResponseEntity.status(403).build();
                }
            } else if ("ROLE_MANAGER".equals(role)) {
                Long deptId = up.getDepartmentId();
                if (deptId == null) return ResponseEntity.status(403).build();
                var empOpt = employeeRepository.findById(employeeId);
                if (empOpt.isEmpty() || empOpt.get().getDepartment() == null || !deptId.equals(empOpt.get().getDepartment().getId())) {
                    return ResponseEntity.status(403).build();
                }
            }
        }
        return ResponseEntity.ok(leaveService.getLeaveBalances(employeeId));
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(@RequestBody Leave leave) {
        try {
            String requester = SecurityContextHolder.getContext().getAuthentication().getName();
            Leave saved = leaveService.applyLeave(leave, requester);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<?> approveOrRejectLeave(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "comments", defaultValue = "") String comments) {
        try {
            String managerUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Leave updated = leaveService.approveOrRejectLeave(id, status, comments, managerUsername);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/accrue")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<?> accrueLeaves() {
        try {
            String hrUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            leaveService.accrueLeavesManually(hrUsername);
            return ResponseEntity.ok(Map.of("message", "Leave accrual completed successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/policies")
    public ResponseEntity<List<LeavePolicy>> getAllPolicies() {
        return ResponseEntity.ok(leaveService.getAllPolicies());
    }

    @PutMapping("/policies/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR','ROLE_ADMIN')")
    public ResponseEntity<?> updatePolicy(@PathVariable Long id, @RequestBody LeavePolicy policyDetails) {
        try {
            String hrUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            LeavePolicy updated = leaveService.updatePolicy(id, policyDetails, hrUsername);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
