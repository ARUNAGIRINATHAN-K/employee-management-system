package com.workforcehub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    public Map<String, Object> calculatePayroll(Long employeeId, BigDecimal baseSalary) {
        log.info("Calculating payroll for employee {} with base salary {}", employeeId, baseSalary);
        
        // Simple mock payroll calculation
        BigDecimal taxDeduction = baseSalary.multiply(new BigDecimal("0.20"));
        BigDecimal benefitsDeduction = new BigDecimal("200.00");
        BigDecimal netPay = baseSalary.subtract(taxDeduction).subtract(benefitsDeduction);
        
        Map<String, Object> payslip = new HashMap<>();
        payslip.put("employeeId", employeeId);
        payslip.put("period", LocalDate.now().getMonth().toString() + " " + LocalDate.now().getYear());
        payslip.put("baseSalary", baseSalary);
        payslip.put("taxDeduction", taxDeduction);
        payslip.put("benefitsDeduction", benefitsDeduction);
        payslip.put("netPay", netPay);
        payslip.put("status", "PROCESSED");
        
        return payslip;
    }
    
    public void generatePayslipPdf(Long payrollId) {
        // To be implemented with iText PDF
        log.info("Generating PDF payslip for payroll run {}", payrollId);
    }
}
