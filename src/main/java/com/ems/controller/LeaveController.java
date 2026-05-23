package com.ems.controller;

import com.ems.entity.Leave;
import com.ems.entity.LeaveBalance;
import com.ems.service.LeaveService;
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

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<List<Leave>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Leave>> getEmployeeLeaves(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesByEmployee(employeeId));
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<List<Leave>> getManagerLeaves(@PathVariable Long managerId) {
        return ResponseEntity.ok(leaveService.getLeavesForManager(managerId));
    }

    @GetMapping("/balances/employee/{employeeId}")
    public ResponseEntity<List<LeaveBalance>> getEmployeeLeaveBalances(@PathVariable Long employeeId) {
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
}
