package com.ems.controller;

import com.ems.entity.Employee;
import com.ems.entity.ExpenseClaim;
import com.ems.entity.User;
import com.ems.repository.UserRepository;
import com.ems.service.ExpenseClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseClaimController {

    @Autowired
    private ExpenseClaimService expenseClaimService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<List<ExpenseClaim>> getAllClaims() {
        return ResponseEntity.ok(expenseClaimService.getAllClaims());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeClaims(@PathVariable Long employeeId) {
        try {
            String requester = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(requester)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Employee employee = user.getEmployee();
            
            // Check authorization: must be either the owner, manager, or HR
            if (employee == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "User has no employee profile"));
            }
            
            boolean isOwner = employee.getId().equals(employeeId);
            boolean isHr = "ROLE_HR".equalsIgnoreCase(user.getRole());
            boolean isManager = "ROLE_MANAGER".equalsIgnoreCase(user.getRole());
            
            if (!isOwner && !isHr && !isManager) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied."));
            }
            
            return ResponseEntity.ok(expenseClaimService.getClaimsByEmployee(employeeId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<List<ExpenseClaim>> getManagerClaims(@PathVariable Long managerId) {
        return ResponseEntity.ok(expenseClaimService.getClaimsForManager(managerId));
    }

    @PostMapping
    public ResponseEntity<?> submitClaim(@RequestBody ExpenseClaim claim) {
        try {
            String requester = SecurityContextHolder.getContext().getAuthentication().getName();
            ExpenseClaim saved = expenseClaimService.submitClaim(claim, requester);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<?> approveClaim(@PathVariable Long id) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            ExpenseClaim updated = expenseClaimService.approveClaim(id, username);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<?> rejectClaim(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String comments = payload.getOrDefault("comments", "Rejected");
            ExpenseClaim updated = expenseClaimService.rejectClaim(id, comments, username);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
