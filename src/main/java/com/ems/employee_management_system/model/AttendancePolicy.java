package com.ems.employee_management_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing organizational shift configurations and grace period rules.
 */
@Entity
@Table(name = "attendance_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_start_time", nullable = false)
    private LocalTime shiftStartTime;

    @Column(name = "shift_end_time", nullable = false)
    private LocalTime shiftEndTime;

    @Column(name = "grace_period_minutes", nullable = false)
    @Builder.Default
    private int gracePeriodMinutes = 15;

    @Column(name = "overtime_threshold_min", nullable = false)
    @Builder.Default
    private int overtimeThresholdMinutes = 60;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
