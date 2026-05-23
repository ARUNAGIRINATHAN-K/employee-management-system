package com.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "profile_change_requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProfileChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee employee;

    @Column(name = "requested_fields_json", nullable = false, length = 1000)
    private String requestedFieldsJson;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    private String comments;

    public ProfileChangeRequest() {}

    public ProfileChangeRequest(Long id, Employee employee, String requestedFieldsJson, String status,
                                LocalDateTime submittedAt, Employee processedBy, LocalDateTime processedAt, String comments) {
        this.id = id;
        this.employee = employee;
        this.requestedFieldsJson = requestedFieldsJson;
        this.status = status != null ? status : "PENDING";
        this.submittedAt = submittedAt != null ? submittedAt : LocalDateTime.now();
        this.processedBy = processedBy;
        this.processedAt = processedAt;
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

    public String getRequestedFieldsJson() {
        return requestedFieldsJson;
    }

    public void setRequestedFieldsJson(String requestedFieldsJson) {
        this.requestedFieldsJson = requestedFieldsJson;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Employee getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Employee processedBy) {
        this.processedBy = processedBy;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public static ProfileChangeRequestBuilder builder() {
        return new ProfileChangeRequestBuilder();
    }

    public static class ProfileChangeRequestBuilder {
        private Long id;
        private Employee employee;
        private String requestedFieldsJson;
        private String status = "PENDING";
        private LocalDateTime submittedAt = LocalDateTime.now();
        private Employee processedBy;
        private LocalDateTime processedAt;
        private String comments;

        public ProfileChangeRequestBuilder id(Long id) { this.id = id; return this; }
        public ProfileChangeRequestBuilder employee(Employee employee) { this.employee = employee; return this; }
        public ProfileChangeRequestBuilder requestedFieldsJson(String requestedFieldsJson) { this.requestedFieldsJson = requestedFieldsJson; return this; }
        public ProfileChangeRequestBuilder status(String status) { this.status = status; return this; }
        public ProfileChangeRequestBuilder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
        public ProfileChangeRequestBuilder processedBy(Employee processedBy) { this.processedBy = processedBy; return this; }
        public ProfileChangeRequestBuilder processedAt(LocalDateTime processedAt) { this.processedAt = processedAt; return this; }
        public ProfileChangeRequestBuilder comments(String comments) { this.comments = comments; return this; }

        public ProfileChangeRequest build() {
            return new ProfileChangeRequest(id, employee, requestedFieldsJson, status, submittedAt, processedBy, processedAt, comments);
        }
    }
}
