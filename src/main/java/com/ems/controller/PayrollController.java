package com.ems.controller;

import com.ems.entity.Payroll;
import com.ems.service.PayrollService;
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

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<Payroll>> getEmployeePayroll(@PathVariable Long employeeId) {
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
        byte[] pdfData = payrollService.generatePayslipPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}
