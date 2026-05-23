package com.ems.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "leave_balances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "leave_type"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "leave_type", nullable = false)
    private String leaveType;

    @Column(nullable = false)
    private Double balance; // Days remaining

    public LeaveBalance() {}

    public LeaveBalance(Long id, Employee employee, String leaveType, Double balance) {
        this.id = id;
        this.employee = employee;
        this.leaveType = leaveType;
        this.balance = balance;
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

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public static LeaveBalanceBuilder builder() {
        return new LeaveBalanceBuilder();
    }

    public static class LeaveBalanceBuilder {
        private Long id;
        private Employee employee;
        private String leaveType;
        private Double balance;

        public LeaveBalanceBuilder id(Long id) { this.id = id; return this; }
        public LeaveBalanceBuilder employee(Employee employee) { this.employee = employee; return this; }
        public LeaveBalanceBuilder leaveType(String leaveType) { this.leaveType = leaveType; return this; }
        public LeaveBalanceBuilder balance(Double balance) { this.balance = balance; return this; }

        public LeaveBalance build() {
            return new LeaveBalance(id, employee, leaveType, balance);
        }
    }
}
