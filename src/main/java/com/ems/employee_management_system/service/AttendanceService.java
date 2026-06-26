package com.ems.employee_management_system.service;

import com.ems.employee_management_system.dto.AttendanceDTO;
import com.ems.employee_management_system.dto.AttendanceSummaryDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for employee punch operations and attendance statistics.
 */
public interface AttendanceService {
    AttendanceDTO clockIn(String username);
    AttendanceDTO clockOut(String username);
    AttendanceDTO getTodayStatus(String username);
    List<AttendanceDTO> getPersonalHistory(String username, LocalDate startDate, LocalDate endDate);
    AttendanceSummaryDTO getPersonalSummary(String username);
    List<AttendanceDTO> getTeamTodayStatus(String managerUsername);
    List<AttendanceDTO> getTeamHistory(String managerUsername, LocalDate startDate, LocalDate endDate);
    Map<String, Long> getTeamSummaryCounts(String managerUsername);
}
