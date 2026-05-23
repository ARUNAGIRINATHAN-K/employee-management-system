package com.ems.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "shifts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "grace_period_minutes", nullable = false)
    private Integer gracePeriodMinutes = 15;

    public Shift() {}

    public Shift(Long id, String name, LocalTime startTime, LocalTime endTime, Integer gracePeriodMinutes) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.gracePeriodMinutes = gracePeriodMinutes != null ? gracePeriodMinutes : 15;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getGracePeriodMinutes() {
        return gracePeriodMinutes;
    }

    public void setGracePeriodMinutes(Integer gracePeriodMinutes) {
        this.gracePeriodMinutes = gracePeriodMinutes;
    }

    public static ShiftBuilder builder() {
        return new ShiftBuilder();
    }

    public static class ShiftBuilder {
        private Long id;
        private String name;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer gracePeriodMinutes = 15;

        public ShiftBuilder id(Long id) { this.id = id; return this; }
        public ShiftBuilder name(String name) { this.name = name; return this; }
        public ShiftBuilder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public ShiftBuilder endTime(LocalTime endTime) { this.endTime = endTime; return this; }
        public ShiftBuilder gracePeriodMinutes(Integer gracePeriodMinutes) { this.gracePeriodMinutes = gracePeriodMinutes; return this; }

        public Shift build() {
            return new Shift(id, name, startTime, endTime, gracePeriodMinutes);
        }
    }
}
