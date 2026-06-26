package com.ems.employee_management_system.controller;

import com.ems.employee_management_system.dto.AttendanceDTO;
import com.ems.employee_management_system.dto.AttendanceSummaryDTO;
import com.ems.employee_management_system.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing employee attendance, punch clock operations, and reporting.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * POST /api/attendance/clock-in : Clock in for the current day.
     */
    @PostMapping("/clock-in")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceDTO> clockIn(Principal principal) {
        return ResponseEntity.ok(attendanceService.clockIn(principal.getName()));
    }

    /**
     * POST /api/attendance/clock-out : Clock out for the current day.
     */
    @PostMapping("/clock-out")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceDTO> clockOut(Principal principal) {
        return ResponseEntity.ok(attendanceService.clockOut(principal.getName()));
    }

    /**
     * GET /api/attendance/today : Retrieve the today's clock in/out status.
     */
    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceDTO> getTodayStatus(Principal principal) {
        return ResponseEntity.ok(attendanceService.getTodayStatus(principal.getName()));
    }

    /**
     * GET /api/attendance/history : Retrieve personal historical logs range.
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttendanceDTO>> getPersonalHistory(
            Principal principal,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getPersonalHistory(principal.getName(), startDate, endDate));
    }

    /**
     * GET /api/attendance/summary : Retrieve personal attendance counts summary (present, absent, etc.).
     */
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceSummaryDTO> getPersonalSummary(Principal principal) {
        return ResponseEntity.ok(attendanceService.getPersonalSummary(principal.getName()));
    }

    /**
     * GET /api/attendance/team/today : Retrieve manager's department team presence list today.
     */
    @GetMapping("/team/today")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<List<AttendanceDTO>> getTeamTodayStatus(Principal principal) {
        return ResponseEntity.ok(attendanceService.getTeamTodayStatus(principal.getName()));
    }

    /**
     * GET /api/attendance/team/history : Retrieve manager's team logs within date range.
     */
    @GetMapping("/team/history")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<List<AttendanceDTO>> getTeamHistory(
            Principal principal,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getTeamHistory(principal.getName(), startDate, endDate));
    }

    /**
     * GET /api/attendance/team/summary : Retrieve manager's team count aggregations (present, late, absent).
     */
    @GetMapping("/team/summary")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<Map<String, Long>> getTeamSummaryCounts(Principal principal) {
        return ResponseEntity.ok(attendanceService.getTeamSummaryCounts(principal.getName()));
    }
}
