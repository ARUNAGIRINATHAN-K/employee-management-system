package com.ems.service;

import com.ems.entity.Employee;
import com.ems.entity.ExpenseClaim;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.ExpenseClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseClaimService {

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<ExpenseClaim> getAllClaims() {
        return expenseClaimRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ExpenseClaim> getClaimsByEmployee(Long employeeId) {
        return expenseClaimRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<ExpenseClaim> getClaimsForManager(Long managerId) {
        return expenseClaimRepository.findByManagerId(managerId);
    }

    @Transactional
    public ExpenseClaim submitClaim(ExpenseClaim claim, String requesterUsername) {
        Employee employee = employeeRepository.findById(claim.getEmployee().getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (claim.getAmount() == null || claim.getAmount() <= 0) {
            throw new RuntimeException("Claim amount must be greater than zero.");
        }

        claim.setEmployee(employee);
        claim.setStatus("PENDING");
        if (claim.getClaimDate() == null) {
            claim.setClaimDate(LocalDate.now());
        }

        ExpenseClaim saved = expenseClaimRepository.save(claim);
        auditLogService.log("EXPENSE_CLAIM_SUBMIT", requesterUsername,
                "Submitted expense claim: " + claim.getTitle() + " for $" + claim.getAmount() + " (ID: " + saved.getId() + ")");
        return saved;
    }

    @Transactional
    public ExpenseClaim approveClaim(Long claimId, String managerUsername) {
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Expense claim not found"));

        if (!"PENDING".equals(claim.getStatus())) {
            throw new RuntimeException("Expense claim is already processed.");
        }

        Employee manager = employeeRepository.findByStatusNot("DELETED").stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(managerUsername))
                .findFirst().orElse(null);

        claim.setStatus("APPROVED");
        claim.setApprovedBy(manager);
        ExpenseClaim updated = expenseClaimRepository.save(claim);

        auditLogService.log("EXPENSE_CLAIM_APPROVE", managerUsername,
                "Approved expense claim ID: " + claimId + " for $" + claim.getAmount());
        return updated;
    }

    @Transactional
    public ExpenseClaim rejectClaim(Long claimId, String comments, String managerUsername) {
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Expense claim not found"));

        if (!"PENDING".equals(claim.getStatus())) {
            throw new RuntimeException("Expense claim is already processed.");
        }

        Employee manager = employeeRepository.findByStatusNot("DELETED").stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(managerUsername))
                .findFirst().orElse(null);

        claim.setStatus("REJECTED");
        claim.setApprovedBy(manager);
        claim.setComments(comments);
        ExpenseClaim updated = expenseClaimRepository.save(claim);

        auditLogService.log("EXPENSE_CLAIM_REJECT", managerUsername,
                "Rejected expense claim ID: " + claimId + " with comment: " + comments);
        return updated;
    }
}
