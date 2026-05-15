package com.workforcehub.service;

import com.workforcehub.dto.response.DashboardResponse;
import com.workforcehub.enums.EmployeeStatus;
import com.workforcehub.enums.LeaveStatus;
import com.workforcehub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final DepartmentRepository departmentRepository;

    public DashboardResponse getDashboardData() {
        long totalEmployees = employeeRepository.countActive();
        long activeEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        long presentToday = attendanceRepository.countPresentToday(LocalDate.now());
        long absentToday = totalEmployees - presentToday;
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long totalDepartments = departmentRepository.findAllActive().size();

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        long newHires = employeeRepository.findByJoiningDateBetween(monthStart, LocalDate.now()).size();

        // Department distribution
        Map<String, Long> deptDist = new LinkedHashMap<>();
        employeeRepository.countByDepartment().forEach(row ->
                deptDist.put((String) row[0], (Long) row[1]));

        // Monthly hiring stats
        Map<String, Long> monthlyHiring = new LinkedHashMap<>();
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        employeeRepository.getMonthlyHiringStats(LocalDate.now().getYear()).forEach(row -> {
            int monthIdx = ((Number) row[0]).intValue() - 1;
            if (monthIdx >= 0 && monthIdx < 12) monthlyHiring.put(months[monthIdx], (Long) row[1]);
        });

        // Attendance trend (last 7 days)
        Map<String, Long> attendanceTrend = new LinkedHashMap<>();
        attendanceRepository.getAttendanceTrend(LocalDate.now().minusDays(7), LocalDate.now()).forEach(row ->
                attendanceTrend.put(row[0].toString(), (Long) row[1]));

        return DashboardResponse.builder()
                .totalEmployees(totalEmployees).activeEmployees(activeEmployees)
                .presentToday(presentToday).absentToday(absentToday)
                .pendingLeaveRequests(pendingLeaves).totalDepartments(totalDepartments)
                .newHiresThisMonth(newHires).departmentDistribution(deptDist)
                .monthlyHiring(monthlyHiring).attendanceTrend(attendanceTrend)
                .build();
    }
}
