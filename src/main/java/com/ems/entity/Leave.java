package com.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "leaves")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee employee;

    @Column(name = "leave_type", nullable = false)
    private String leaveType; // CASUAL, SICK, EARNED, etc.

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private String reason;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee approvedBy;

    private String comments;

    public Leave() {}

    public Leave(Long id, Employee employee, String leaveType, LocalDate startDate, LocalDate endDate, String reason, String status, Employee approvedBy, String comments) {
        this.id = id;
        this.employee = employee;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status != null ? status : "PENDING";
        this.approvedBy = approvedBy;
        this.comments = comments;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Employee getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Employee approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public static LeaveBuilder builder() {
        return new LeaveBuilder();
    }

    public static class LeaveBuilder {
        private Long id;
        private Employee employee;
        private String leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private String status = "PENDING";
        private Employee approvedBy;
        private String comments;

        public LeaveBuilder id(Long id) { this.id = id; return this; }
        public LeaveBuilder employee(Employee employee) { this.employee = employee; return this; }
        public LeaveBuilder leaveType(String leaveType) { this.leaveType = leaveType; return this; }
        public LeaveBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public LeaveBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public LeaveBuilder reason(String reason) { this.reason = reason; return this; }
        public LeaveBuilder status(String status) { this.status = status; return this; }
        public LeaveBuilder approvedBy(Employee approvedBy) { this.approvedBy = approvedBy; return this; }
        public LeaveBuilder comments(String comments) { this.comments = comments; return this; }

        public Leave build() {
            return new Leave(id, employee, leaveType, startDate, endDate, reason, status, approvedBy, comments);
        }
    }
}
