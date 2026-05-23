package com.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "expense_claims")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExpenseClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee employee;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String category; // TRAVEL, MEALS, EQUIPMENT, OTHER

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, PAID

    @Column(name = "claim_date", nullable = false)
    private LocalDate claimDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee approvedBy;

    private String comments;

    public ExpenseClaim() {}

    public ExpenseClaim(Long id, Employee employee, String title, Double amount, String category,
                        String status, LocalDate claimDate, Employee approvedBy, String comments) {
        this.id = id;
        this.employee = employee;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.status = status != null ? status : "PENDING";
        this.claimDate = claimDate != null ? claimDate : LocalDate.now();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
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

    public static ExpenseClaimBuilder builder() {
        return new ExpenseClaimBuilder();
    }

    public static class ExpenseClaimBuilder {
        private Long id;
        private Employee employee;
        private String title;
        private Double amount;
        private String category;
        private String status = "PENDING";
        private LocalDate claimDate = LocalDate.now();
        private Employee approvedBy;
        private String comments;

        public ExpenseClaimBuilder id(Long id) { this.id = id; return this; }
        public ExpenseClaimBuilder employee(Employee employee) { this.employee = employee; return this; }
        public ExpenseClaimBuilder title(String title) { this.title = title; return this; }
        public ExpenseClaimBuilder amount(Double amount) { this.amount = amount; return this; }
        public ExpenseClaimBuilder category(String category) { this.category = category; return this; }
        public ExpenseClaimBuilder status(String status) { this.status = status; return this; }
        public ExpenseClaimBuilder claimDate(LocalDate claimDate) { this.claimDate = claimDate; return this; }
        public ExpenseClaimBuilder approvedBy(Employee approvedBy) { this.approvedBy = approvedBy; return this; }
        public ExpenseClaimBuilder comments(String comments) { this.comments = comments; return this; }

        public ExpenseClaim build() {
            return new ExpenseClaim(id, employee, title, amount, category, status, claimDate, approvedBy, comments);
        }
    }
}
