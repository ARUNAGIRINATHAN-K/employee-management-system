package com.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "payroll", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "pay_period"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee employee;

    @Column(name = "pay_period", nullable = false)
    private String payPeriod; // Format: YYYY-MM

    @Column(name = "basic_salary", nullable = false)
    private Double basicSalary;

    private Double allowances;

    private Double deductions;

    @Column(name = "net_salary", nullable = false)
    private Double netSalary;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, PAID

    @Column(name = "processed_date")
    private LocalDate processedDate;

    public Payroll() {}

    public Payroll(Long id, Employee employee, String payPeriod, Double basicSalary, Double allowances, Double deductions, Double netSalary, String status, LocalDate processedDate) {
        this.id = id;
        this.employee = employee;
        this.payPeriod = payPeriod;
        this.basicSalary = basicSalary;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netSalary = netSalary;
        this.status = status != null ? status : "PENDING";
        this.processedDate = processedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(String payPeriod) {
        this.payPeriod = payPeriod;
    }

    public Double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(Double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public Double getAllowances() {
        return allowances;
    }

    public void setAllowances(Double allowances) {
        this.allowances = allowances;
    }

    public Double getDeductions() {
        return deductions;
    }

    public void setDeductions(Double deductions) {
        this.deductions = deductions;
    }

    public Double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(Double netSalary) {
        this.netSalary = netSalary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDate processedDate) {
        this.processedDate = processedDate;
    }

    public static PayrollBuilder builder() {
        return new PayrollBuilder();
    }

    public static class PayrollBuilder {
        private Long id;
        private Employee employee;
        private String payPeriod;
        private Double basicSalary;
        private Double allowances;
        private Double deductions;
        private Double netSalary;
        private String status = "PENDING";
        private LocalDate processedDate;

        public PayrollBuilder id(Long id) { this.id = id; return this; }
        public PayrollBuilder employee(Employee employee) { this.employee = employee; return this; }
        public PayrollBuilder payPeriod(String payPeriod) { this.payPeriod = payPeriod; return this; }
        public PayrollBuilder basicSalary(Double basicSalary) { this.basicSalary = basicSalary; return this; }
        public PayrollBuilder allowances(Double allowances) { this.allowances = allowances; return this; }
        public PayrollBuilder deductions(Double deductions) { this.deductions = deductions; return this; }
        public PayrollBuilder netSalary(Double netSalary) { this.netSalary = netSalary; return this; }
        public PayrollBuilder status(String status) { this.status = status; return this; }
        public PayrollBuilder processedDate(LocalDate processedDate) { this.processedDate = processedDate; return this; }

        public Payroll build() {
            return new Payroll(id, employee, payPeriod, basicSalary, allowances, deductions, netSalary, status, processedDate);
        }
    }
}
