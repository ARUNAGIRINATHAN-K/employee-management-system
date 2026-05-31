package com.ems.controller;

import com.ems.entity.Payroll;
import com.ems.service.PayrollService;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.PayrollRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<Payroll>> getEmployeePayroll(@PathVariable Long employeeId) {
        var principal = SecurityContextHolder.getContext().getAuthentication();
        Long actingEmployeeId = null;
        if (principal != null && principal.getPrincipal() instanceof com.ems.security.UserPrincipal) {
            var up = ((com.ems.security.UserPrincipal) principal.getPrincipal());
            if (up.getUser() != null && up.getUser().getEmployee() != null) {
                actingEmployeeId = up.getUser().getEmployee().getId();
            }
        }
        if (actingEmployeeId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!actingEmployeeId.equals(employeeId)) {
            var target = employeeRepository.findById(employeeId).orElse(null);
            var acting = employeeRepository.findById(actingEmployeeId).orElse(null);
            boolean allowed = false;
            if (acting != null && ("ROLE_HR".equals(principal.getAuthorities().stream().findFirst().map(Object::toString).orElse(null)))) allowed = true;
            if (acting != null && acting.getId().equals(target != null && target.getManager() != null ? target.getManager().getId() : null)) allowed = true;
            if (!allowed) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(payrollService.getPayrollHistory(employeeId));
    }

    @GetMapping("/period")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<List<Payroll>> getPayrollByPeriod(@RequestParam("period") String payPeriod) {
        return ResponseEntity.ok(payrollService.getPayrollByPeriod(payPeriod));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<?> generatePayroll(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("payPeriod") String payPeriod) {
        try {
            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Payroll generated = payrollService.generatePayroll(employeeId, payPeriod, adminUsername);
            return ResponseEntity.ok(generated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<?> paySalary(@PathVariable Long id) {
        try {
            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Payroll paid = payrollService.markAsPaid(id, adminUsername);
            return ResponseEntity.ok(paid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/payslip")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Long id) {
        var principal = SecurityContextHolder.getContext().getAuthentication();
        Long actingEmployeeId = null;
        if (principal != null && principal.getPrincipal() instanceof com.ems.security.UserPrincipal) {
            var up = ((com.ems.security.UserPrincipal) principal.getPrincipal());
            if (up.getUser() != null && up.getUser().getEmployee() != null) {
                actingEmployeeId = up.getUser().getEmployee().getId();
            }
        }
        if (actingEmployeeId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Payroll payroll = payrollRepository.findById(id).orElse(null);
        if (payroll == null) return ResponseEntity.notFound().build();
        Long ownerId = payroll.getEmployee() != null ? payroll.getEmployee().getId() : null;
        if (!actingEmployeeId.equals(ownerId)) {
            var acting = employeeRepository.findById(actingEmployeeId).orElse(null);
            boolean allowed = false;
            if (acting != null && ("ROLE_HR".equals(principal.getAuthorities().stream().findFirst().map(Object::toString).orElse(null)))) allowed = true;
            if (acting != null && acting.getId().equals(employeeRepository.findById(ownerId).map(e->e.getManager() != null ? e.getManager().getId() : null).orElse(null))) allowed = true;
            if (!allowed) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        byte[] pdfData = payrollService.generatePayslipPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}
