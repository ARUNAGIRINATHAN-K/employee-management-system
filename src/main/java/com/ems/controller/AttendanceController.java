package com.ems.controller;

import com.ems.entity.Attendance;
import com.ems.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/check-in/{employeeId}")
    public ResponseEntity<?> checkIn(@PathVariable Long employeeId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Attendance attendance = attendanceService.checkIn(employeeId, username);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/check-out/{employeeId}")
    public ResponseEntity<?> checkOut(@PathVariable Long employeeId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Attendance attendance = attendanceService.checkOut(employeeId, username);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/today/{employeeId}")
    public ResponseEntity<?> getTodayAttendance(@PathVariable Long employeeId) {
        return attendanceService.getTodayAttendance(employeeId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of("status", "NOT_CHECKED_IN")));
    }

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<Attendance>> getHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendanceHistory(employeeId));
    }

    @GetMapping("/date")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<List<Attendance>> getByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }
}
