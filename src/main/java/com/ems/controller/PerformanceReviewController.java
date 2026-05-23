package com.ems.controller;

import com.ems.entity.PerformanceReview;
import com.ems.service.PerformanceReviewService;
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

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PerformanceReview>> getEmployeeReviews(@PathVariable Long employeeId) {
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
