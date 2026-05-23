package com.ems.service;

import com.ems.entity.Employee;
import com.ems.entity.PerformanceReview;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class PerformanceReviewService {

    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<PerformanceReview> getReviewsByEmployee(Long employeeId) {
        return performanceReviewRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<PerformanceReview> getReviewsByReviewer(Long reviewerId) {
        return performanceReviewRepository.findByReviewerId(reviewerId);
    }

    @Transactional
    public PerformanceReview addReview(PerformanceReview review, String reviewerUsername) {
        Employee employee = employeeRepository.findById(review.getEmployee().getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Employee reviewer = employeeRepository.findById(review.getReviewer().getId())
                .orElseThrow(() -> new RuntimeException("Reviewer employee profile not found"));

        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setReviewDate(LocalDate.now());

        PerformanceReview saved = performanceReviewRepository.save(review);
        auditLogService.log("ADD_PERFORMANCE_REVIEW", reviewerUsername, 
                "Added review rating " + review.getRating() + "/5 for Employee ID: " + employee.getId());
        return saved;
    }
}
