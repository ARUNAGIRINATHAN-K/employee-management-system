package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.model.*;
import com.ems.employee_management_system.repository.AttendancePolicyRepository;
import com.ems.employee_management_system.repository.AttendanceRepository;
import com.ems.employee_management_system.repository.EmployeeRepository;
import com.ems.employee_management_system.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Nightly scheduled job to evaluate and finalize daily attendance for all active employees.
 */
@Component
@RequiredArgsConstructor
public class DailyAttendanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyAttendanceScheduler.class);

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendancePolicyRepository policyRepository;

    /**
     * Nightly finalization task running at 11:59 PM every day.
     */
    @Scheduled(cron = "0 59 23 * * ?")
    @Transactional
    public void finalizeDailyAttendance() {
        LocalDate today = LocalDate.now();
        log.info("Starting Daily Auto-Attendance Finalization Scheduler for date: {}", today);
        finalizeAttendanceForDate(today);
        log.info("Daily Auto-Attendance Finalization Scheduler completed successfully.");
    }

    /**
     * Core finalization logic for a specific date (allows manual testing or recovery triggers).
     */
    @Transactional
    public void finalizeAttendanceForDate(LocalDate date) {
        // Fetch all active employees
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .collect(Collectors.toList());

        AttendancePolicy policy = policyRepository.findById(1L)
                .orElse(AttendancePolicy.builder()
                        .shiftStartTime(LocalTime.of(9, 0))
                        .shiftEndTime(LocalTime.of(17, 0))
                        .gracePeriodMinutes(15)
                        .overtimeThresholdMinutes(60)
                        .build());

        for (Employee employee : activeEmployees) {
            try {
                processEmployeeAttendanceForDate(employee, date, policy);
            } catch (Exception e) {
                log.error("Failed to finalize attendance for employee id: {} on date: {}. Reason: {}", 
                        employee.getId(), date, e.getMessage());
            }
        }
    }

    private void processEmployeeAttendanceForDate(Employee employee, LocalDate date, AttendancePolicy policy) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), date);

        if (attendanceOpt.isPresent()) {
            Attendance attendance = attendanceOpt.get();

            // Case: Clocked in but forgot to clock out at end of day
            if (attendance.getClockIn() != null && attendance.getClockOut() == null) {
                log.info("Auto-clocking out employee id: {} for date: {}", employee.getId(), date);
                LocalDateTime autoClockOutTime = LocalDateTime.of(date, policy.getShiftEndTime());
                attendance.setClockOut(autoClockOutTime);

                // Overtime calculation
                LocalTime shiftEnd = policy.getShiftEndTime();
                LocalTime clockOutTime = autoClockOutTime.toLocalTime();
                int overtimeMin = 0;
                if (clockOutTime.isAfter(shiftEnd.plusMinutes(policy.getOvertimeThresholdMinutes()))) {
                    overtimeMin = (int) ChronoUnit.MINUTES.between(shiftEnd, clockOutTime);
                }
                attendance.setOvertimeMinutes(overtimeMin);
                attendanceRepository.save(attendance);
            }
            return;
        }

        // Case: No existing attendance record for today (No Clock-In)
        AttendanceStatus status = AttendanceStatus.ABSENT;
        WorkMode workMode = WorkMode.OFFICE;

        // 1. Weekend Check
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            status = AttendanceStatus.WEEKEND;
        }
        // 2. Public Holiday Check
        else if (isPublicHoliday(date)) {
            status = AttendanceStatus.HOLIDAY;
        }
        // 3. Approved Leave / WFH Check
        else {
            List<LeaveRequest> approvedRequests = leaveRequestRepository
                    .findApprovedLeaveByEmployeeIdAndDate(employee.getId(), date);

            Optional<LeaveRequest> leaveOpt = approvedRequests.stream()
                    .filter(l -> l.getLeaveType() != LeaveType.WFH)
                    .findFirst();

            Optional<LeaveRequest> wfhOpt = approvedRequests.stream()
                    .filter(l -> l.getLeaveType() == LeaveType.WFH)
                    .findFirst();

            if (leaveOpt.isPresent()) {
                status = AttendanceStatus.ON_LEAVE;
            } else if (wfhOpt.isPresent()) {
                status = AttendanceStatus.WFH;
                workMode = WorkMode.REMOTE;
            }
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(date)
                .status(status)
                .workMode(workMode)
                .lateMinutes(0)
                .overtimeMinutes(0)
                .build();

        attendanceRepository.save(attendance);
    }

    private boolean isPublicHoliday(LocalDate date) {
        // Core standard holidays list (mock holiday calendar)
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // Jan 1: New Year
        if (month == 1 && day == 1) return true;
        // Jan 26: Republic Day
        if (month == 1 && day == 26) return true;
        // Aug 15: Independence Day
        if (month == 8 && day == 15) return true;
        // Oct 2: Gandhi Jayanti
        if (month == 10 && day == 2) return true;
        // Dec 25: Christmas
        if (month == 12 && day == 25) return true;

        return false;
    }
}
