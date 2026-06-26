package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.dto.AttendanceDTO;
import com.ems.employee_management_system.dto.AttendanceSummaryDTO;
import com.ems.employee_management_system.exception.BadRequestException;
import com.ems.employee_management_system.exception.ResourceNotFoundException;
import com.ems.employee_management_system.mapper.AttendanceMapper;
import com.ems.employee_management_system.model.*;
import com.ems.employee_management_system.repository.*;
import com.ems.employee_management_system.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing employee attendance logs and punch clocks.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendancePolicyRepository policyRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public AttendanceDTO clockIn(String username) {
        Employee employee = getEmployeeByUsername(username);
        LocalDate today = LocalDate.now();

        Optional<Attendance> existingOpt = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today);
        if (existingOpt.isPresent()) {
            throw new BadRequestException("You have already clocked in for today.");
        }

        AttendancePolicy policy = policyRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Default shift policy not configured"));

        // Determine if WFH request is approved for today
        List<LeaveRequest> approvedLeaves = leaveRequestRepository
                .findApprovedLeaveByEmployeeIdAndDate(employee.getId(), today);
        boolean isApprovedWfh = approvedLeaves.stream()
                .anyMatch(l -> l.getLeaveType() == LeaveType.WFH);

        LocalDateTime now = LocalDateTime.now();
        LocalTime nowTime = now.toLocalTime();
        int lateMin = 0;
        AttendanceStatus status = AttendanceStatus.PRESENT;

        // If clocking in after shiftStartTime + gracePeriod
        LocalTime shiftStart = policy.getShiftStartTime();
        LocalTime graceDeadline = shiftStart.plusMinutes(policy.getGracePeriodMinutes());

        if (nowTime.isAfter(graceDeadline)) {
            status = AttendanceStatus.LATE;
            lateMin = (int) ChronoUnit.MINUTES.between(shiftStart, nowTime);
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .clockIn(now)
                .status(status)
                .workMode(isApprovedWfh ? WorkMode.REMOTE : WorkMode.OFFICE)
                .lateMinutes(lateMin)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public AttendanceDTO clockOut(String username) {
        Employee employee = getEmployeeByUsername(username);
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElseThrow(() -> new BadRequestException("You must clock in first before clocking out."));

        if (attendance.getClockOut() != null) {
            throw new BadRequestException("You have already clocked out for today.");
        }

        AttendancePolicy policy = policyRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Default shift policy not configured"));

        LocalDateTime now = LocalDateTime.now();
        LocalTime nowTime = now.toLocalTime();
        int overtimeMin = 0;

        LocalTime shiftEnd = policy.getShiftEndTime();
        LocalTime overtimeThreshold = shiftEnd.plusMinutes(policy.getOvertimeThresholdMinutes());

        if (nowTime.isAfter(overtimeThreshold)) {
            overtimeMin = (int) ChronoUnit.MINUTES.between(shiftEnd, nowTime);
        }

        attendance.setClockOut(now);
        attendance.setOvertimeMinutes(overtimeMin);

        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toDTO(saved);
    }

    @Override
    public AttendanceDTO getTodayStatus(String username) {
        Employee employee = getEmployeeByUsername(username);
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .map(attendanceMapper::toDTO)
                .orElse(null);
    }

    @Override
    public List<AttendanceDTO> getPersonalHistory(String username, LocalDate startDate, LocalDate endDate) {
        Employee employee = getEmployeeByUsername(username);
        return attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(employee.getId(), startDate, endDate)
                .stream()
                .map(attendanceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceSummaryDTO getPersonalSummary(String username) {
        Employee employee = getEmployeeByUsername(username);
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        List<Attendance> logs = attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(
                employee.getId(), startOfMonth, endOfMonth);

        int present = 0;
        int late = 0;
        int absent = 0;
        int leave = 0;
        int wfh = 0;
        int weekend = 0;
        int holiday = 0;
        int totalOvertime = 0;
        int totalLate = 0;

        for (Attendance log : logs) {
            totalOvertime += log.getOvertimeMinutes();
            totalLate += log.getLateMinutes();

            switch (log.getStatus()) {
                case PRESENT:
                    present++;
                    if (log.getWorkMode() == WorkMode.REMOTE) wfh++;
                    break;
                case LATE:
                    late++;
                    if (log.getWorkMode() == WorkMode.REMOTE) wfh++;
                    break;
                case ABSENT:
                    absent++;
                    break;
                case ON_LEAVE:
                    leave++;
                    break;
                case WFH:
                    wfh++;
                    break;
                case WEEKEND:
                    weekend++;
                    break;
                case HOLIDAY:
                    holiday++;
                    break;
            }
        }

        return AttendanceSummaryDTO.builder()
                .presentDays(present)
                .lateDays(late)
                .absentDays(absent)
                .leaveDays(leave)
                .wfhDays(wfh)
                .weekendDays(weekend)
                .holidayDays(holiday)
                .totalOvertimeMinutes(totalOvertime)
                .totalLateMinutes(totalLate)
                .build();
    }

    @Override
    public List<AttendanceDTO> getTeamTodayStatus(String managerUsername) {
        Employee manager = getEmployeeByUsername(managerUsername);
        if (manager.getDepartment() == null) {
            throw new BadRequestException("Manager is not assigned to any department.");
        }
        return attendanceRepository.findByDepartmentIdAndDate(manager.getDepartment().getId(), LocalDate.now())
                .stream()
                .map(attendanceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getTeamHistory(String managerUsername, LocalDate startDate, LocalDate endDate) {
        Employee manager = getEmployeeByUsername(managerUsername);
        if (manager.getDepartment() == null) {
            throw new BadRequestException("Manager is not assigned to any department.");
        }
        return attendanceRepository.findByDepartmentIdAndDateBetween(manager.getDepartment().getId(), startDate, endDate)
                .stream()
                .map(attendanceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getTeamSummaryCounts(String managerUsername) {
        Employee manager = getEmployeeByUsername(managerUsername);
        if (manager.getDepartment() == null) {
            throw new BadRequestException("Manager is not assigned to any department.");
        }

        List<Object[]> queryResults = attendanceRepository.countStatusByDepartmentIdAndDate(
                manager.getDepartment().getId(), LocalDate.now());

        Map<String, Long> statusCounts = new HashMap<>();
        // Initialize counts to 0
        for (AttendanceStatus status : AttendanceStatus.values()) {
            statusCounts.put(status.name(), 0L);
        }

        for (Object[] row : queryResults) {
            AttendanceStatus status = (AttendanceStatus) row[0];
            Long count = (Long) row[1];
            statusCounts.put(status.name(), count);
        }

        return statusCounts;
    }

    private Employee getEmployeeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user: " + username));
    }
}
