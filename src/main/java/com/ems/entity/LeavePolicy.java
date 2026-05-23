package com.ems.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_policies")
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leave_type", nullable = false, unique = true)
    private String leaveType; // e.g. CASUAL, SICK, EARNED

    @Column(name = "annual_allocation", nullable = false)
    private Double annualAllocation;

    @Column(name = "monthly_accrual_rate", nullable = false)
    private Double monthlyAccrualRate;

    public LeavePolicy() {}

    public LeavePolicy(Long id, String leaveType, Double annualAllocation, Double monthlyAccrualRate) {
        this.id = id;
        this.leaveType = leaveType;
        this.annualAllocation = annualAllocation;
        this.monthlyAccrualRate = monthlyAccrualRate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public Double getAnnualAllocation() {
        return annualAllocation;
    }

    public void setAnnualAllocation(Double annualAllocation) {
        this.annualAllocation = annualAllocation;
    }

    public Double getMonthlyAccrualRate() {
        return monthlyAccrualRate;
    }

    public void setMonthlyAccrualRate(Double monthlyAccrualRate) {
        this.monthlyAccrualRate = monthlyAccrualRate;
    }

    public static LeavePolicyBuilder builder() {
        return new LeavePolicyBuilder();
    }

    public static class LeavePolicyBuilder {
        private Long id;
        private String leaveType;
        private Double annualAllocation;
        private Double monthlyAccrualRate;

        public LeavePolicyBuilder id(Long id) { this.id = id; return this; }
        public LeavePolicyBuilder leaveType(String leaveType) { this.leaveType = leaveType; return this; }
        public LeavePolicyBuilder annualAllocation(Double annualAllocation) { this.annualAllocation = annualAllocation; return this; }
        public LeavePolicyBuilder monthlyAccrualRate(Double monthlyAccrualRate) { this.monthlyAccrualRate = monthlyAccrualRate; return this; }

        public LeavePolicy build() {
            return new LeavePolicy(id, leaveType, annualAllocation, monthlyAccrualRate);
        }
    }
}
