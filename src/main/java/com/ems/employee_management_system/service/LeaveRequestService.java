package com.ems.employee_management_system.service;

import com.ems.employee_management_system.dto.LeaveRequestDTO;
import java.util.List;

/**
 * Service interface for managing WFH and leave requests lifecycle.
 */
public interface LeaveRequestService {
    LeaveRequestDTO applyLeave(String username, LeaveRequestDTO dto);
    List<LeaveRequestDTO> getPersonalLeaveHistory(String username);
    List<LeaveRequestDTO> getPendingLeaveRequests(String managerUsername);
    LeaveRequestDTO approveLeave(Long id, String managerUsername);
    LeaveRequestDTO rejectLeave(Long id, String managerUsername);
}
