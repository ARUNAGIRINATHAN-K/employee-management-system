package com.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "date"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(nullable = false)
    private String status; // PRESENT, ABSENT, LATE, EARLY_DEPARTURE

    private String notes;

    public Attendance() {}

    public Attendance(Long id, Employee employee, LocalDate date, LocalDateTime checkIn, LocalDateTime checkOut, String status, String notes) {
        this.id = id;
        this.employee = employee;
        this.date = date;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.notes = notes;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public static AttendanceBuilder builder() {
        return new AttendanceBuilder();
    }

    public static class AttendanceBuilder {
        private Long id;
        private Employee employee;
        private LocalDate date;
        private LocalDateTime checkIn;
        private LocalDateTime checkOut;
        private String status;
        private String notes;

        public AttendanceBuilder id(Long id) { this.id = id; return this; }
        public AttendanceBuilder employee(Employee employee) { this.employee = employee; return this; }
        public AttendanceBuilder date(LocalDate date) { this.date = date; return this; }
        public AttendanceBuilder checkIn(LocalDateTime checkIn) { this.checkIn = checkIn; return this; }
        public AttendanceBuilder checkOut(LocalDateTime checkOut) { this.checkOut = checkOut; return this; }
        public AttendanceBuilder status(String status) { this.status = status; return this; }
        public AttendanceBuilder notes(String notes) { this.notes = notes; return this; }

        public Attendance build() {
            return new Attendance(id, employee, date, checkIn, checkOut, status, notes);
        }
    }
}
