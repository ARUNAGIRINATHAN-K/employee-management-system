package com.ems.controller;

import com.ems.entity.Attendance;
import com.ems.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import com.ems.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
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
    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/check-in/{employeeId}")
    public ResponseEntity<?> checkIn(@PathVariable Long employeeId) {
        var principal = SecurityContextHolder.getContext().getAuthentication();
        Long actingEmployeeId = null;
        if (principal != null && principal.getPrincipal() instanceof com.ems.security.UserPrincipal) {
            var up = ((com.ems.security.UserPrincipal) principal.getPrincipal());
            if (up.getUser() != null && up.getUser().getEmployee() != null) {
                actingEmployeeId = up.getUser().getEmployee().getId();
            }
        }
        if (actingEmployeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if (!actingEmployeeId.equals(employeeId)) {
            // check if manager of that employee
            var opt = employeeRepository.findById(employeeId);
            if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
            var target = opt.get();
            var acting = employeeRepository.findById(actingEmployeeId).orElse(null);
            boolean allowed = false;
            if (acting != null && ("ROLE_HR".equals(principal.getAuthorities().stream().findFirst().map(Object::toString).orElse(null)))) {
                allowed = true;
            }
            if (acting != null && acting.getId().equals(target.getManager() != null ? target.getManager().getId() : null)) allowed = true;
            if (!allowed) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        Attendance attendance = attendanceService.checkIn(employeeId, principal.getName());
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/check-out/{employeeId}")
    public ResponseEntity<?> checkOut(@PathVariable Long employeeId) {
        var principal = SecurityContextHolder.getContext().getAuthentication();
        Long actingEmployeeId = null;
        if (principal != null && principal.getPrincipal() instanceof com.ems.security.UserPrincipal) {
            var up = ((com.ems.security.UserPrincipal) principal.getPrincipal());
            if (up.getUser() != null && up.getUser().getEmployee() != null) {
                actingEmployeeId = up.getUser().getEmployee().getId();
            }
        }
        if (actingEmployeeId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        Attendance attendance = attendanceService.checkOut(employeeId, principal.getName());
        Long targetEmpId = attendance.getEmployee() != null ? attendance.getEmployee().getId() : null;
        if (!actingEmployeeId.equals(targetEmpId)) {
            var target = employeeRepository.findById(targetEmpId).orElse(null);
            var acting = employeeRepository.findById(actingEmployeeId).orElse(null);
            boolean allowed = false;
            if (acting != null && ("ROLE_HR".equals(principal.getAuthorities().stream().findFirst().map(Object::toString).orElse(null)))) {
                allowed = true;
            }
            if (acting != null && acting.getId().equals(target != null && target.getManager() != null ? target.getManager().getId() : null)) allowed = true;
            if (!allowed) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/today/{employeeId}")
    public ResponseEntity<?> getTodayAttendance(@PathVariable Long employeeId) {
        var principal = SecurityContextHolder.getContext().getAuthentication();
        Long actingEmployeeId = null;
        if (principal != null && principal.getPrincipal() instanceof com.ems.security.UserPrincipal) {
            var up = ((com.ems.security.UserPrincipal) principal.getPrincipal());
            if (up.getUser() != null && up.getUser().getEmployee() != null) {
                actingEmployeeId = up.getUser().getEmployee().getId();
            }
        }
        if (actingEmployeeId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        if (!actingEmployeeId.equals(employeeId)) {
            var target = employeeRepository.findById(employeeId).orElse(null);
            var acting = employeeRepository.findById(actingEmployeeId).orElse(null);
            boolean allowed = false;
            if (acting != null && ("ROLE_HR".equals(principal.getAuthorities().stream().findFirst().map(Object::toString).orElse(null)))) allowed = true;
            if (acting != null && acting.getId().equals(target != null && target.getManager() != null ? target.getManager().getId() : null)) allowed = true;
            if (!allowed) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        return attendanceService.getTodayAttendance(employeeId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of("status", "NOT_CHECKED_IN")));
    }

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<?> getHistory(@PathVariable Long employeeId) {
        var principal = SecurityContextHolder.getContext().getAuthentication();
        Long actingEmployeeId = null;
        if (principal != null && principal.getPrincipal() instanceof com.ems.security.UserPrincipal) {
            var up = ((com.ems.security.UserPrincipal) principal.getPrincipal());
            if (up.getUser() != null && up.getUser().getEmployee() != null) {
                actingEmployeeId = up.getUser().getEmployee().getId();
            }
        }
        if (actingEmployeeId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        if (!actingEmployeeId.equals(employeeId)) {
            var target = employeeRepository.findById(employeeId).orElse(null);
            var acting = employeeRepository.findById(actingEmployeeId).orElse(null);
            boolean allowed = false;
            if (acting != null && ("ROLE_HR".equals(principal.getAuthorities().stream().findFirst().map(Object::toString).orElse(null)))) allowed = true;
            if (acting != null && acting.getId().equals(target != null && target.getManager() != null ? target.getManager().getId() : null)) allowed = true;
            if (!allowed) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        return ResponseEntity.ok(attendanceService.getEmployeeAttendanceHistory(employeeId));
    }

    @GetMapping("/date")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<List<Attendance>> getByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }
}
