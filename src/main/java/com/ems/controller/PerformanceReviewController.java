package com.ems.controller;

import com.ems.entity.PerformanceReview;
import com.ems.service.PerformanceReviewService;
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
@RequestMapping("/api/performance")
public class PerformanceReviewController {

    @Autowired
    private PerformanceReviewService performanceReviewService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PerformanceReview>> getEmployeeReviews(@PathVariable Long employeeId) {
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
        return ResponseEntity.ok(performanceReviewService.getReviewsByEmployee(employeeId));
    }

    @GetMapping("/reviewer/{reviewerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<List<PerformanceReview>> getReviewerReviews(@PathVariable Long reviewerId) {
        return ResponseEntity.ok(performanceReviewService.getReviewsByReviewer(reviewerId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<?> addReview(@RequestBody PerformanceReview review) {
        try {
            String reviewerUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            PerformanceReview saved = performanceReviewService.addReview(review, reviewerUsername);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
