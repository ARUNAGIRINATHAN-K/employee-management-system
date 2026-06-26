package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.dto.LeaveRequestDTO;
import com.ems.employee_management_system.exception.BadRequestException;
import com.ems.employee_management_system.exception.ResourceNotFoundException;
import com.ems.employee_management_system.mapper.LeaveRequestMapper;
import com.ems.employee_management_system.model.*;
import com.ems.employee_management_system.repository.AttendanceRepository;
import com.ems.employee_management_system.repository.EmployeeRepository;
import com.ems.employee_management_system.repository.LeaveRequestRepository;
import com.ems.employee_management_system.repository.UserRepository;
import com.ems.employee_management_system.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing WFH and leave requests lifecycle.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    @Override
    @Transactional
    public LeaveRequestDTO applyLeave(String username, LeaveRequestDTO dto) {
        Employee employee = getEmployeeByUsername(username);

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date.");
        }

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(dto);
        leaveRequest.setEmployee(employee);

        User applicantUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        boolean isApplicantAdmin = applicantUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (isApplicantAdmin) {
            leaveRequest.setStatus(LeaveStatus.APPROVED);
            leaveRequest.setApprovedBy(applicantUser);
        } else {
            leaveRequest.setStatus(LeaveStatus.PENDING);
        }

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        if (isApplicantAdmin) {
            populateAttendanceForApprovedLeave(saved);
        }

        return leaveRequestMapper.toDTO(saved);
    }

    @Override
    public List<LeaveRequestDTO> getPersonalLeaveHistory(String username) {
        Employee employee = getEmployeeByUsername(username);
        return leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(employee.getId())
                .stream()
                .map(leaveRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDTO> getPendingLeaveRequests(String managerUsername) {
        User managerUser = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Manager user not found"));

        boolean isAdmin = managerUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (isAdmin) {
            // Admin can only approve manager leave requests
            return leaveRequestRepository.findByStatusOrderByStartDateDesc(LeaveStatus.PENDING)
                    .stream()
                    .filter(r -> r.getEmployee().getUser().getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_MANAGER")))
                    .map(leaveRequestMapper::toDTO)
                    .collect(Collectors.toList());
        }

        boolean isManager = managerUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_MANAGER"));

        if (isManager) {
            Employee manager = employeeRepository.findByUserId(managerUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager employee profile not found"));

            if (manager.getDepartment() == null) {
                throw new BadRequestException("Manager is not assigned to any department.");
            }

            return leaveRequestRepository.findByDepartmentIdAndStatus(manager.getDepartment().getId(), LeaveStatus.PENDING)
                    .stream()
                    .filter(r -> {
                        User userOfRequest = r.getEmployee().getUser();
                        boolean isReqManager = userOfRequest.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_MANAGER"));
                        boolean isReqAdmin = userOfRequest.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
                        return !isReqManager && !isReqAdmin; // Only plain employees
                    })
                    .map(leaveRequestMapper::toDTO)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Override
    @Transactional
    public LeaveRequestDTO approveLeave(Long id, String managerUsername) {
        User managerUser = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Manager user not found"));

        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already processed.");
        }

        User applicantUser = request.getEmployee().getUser();
        boolean isApplicantManager = applicantUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_MANAGER"));
        boolean isApplicantAdmin = applicantUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isPlainEmployee = !isApplicantManager && !isApplicantAdmin;

        if (isPlainEmployee) {
            // Must be approved by a Manager in the same department
            boolean isApproverManager = managerUser.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_MANAGER"));
            if (!isApproverManager) {
                throw new AccessDeniedException("Access denied: Employee leave requests can only be approved by their department manager, not by the admin.");
            }
            Employee manager = employeeRepository.findByUserId(managerUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager employee profile not found"));
            if (manager.getDepartment() == null || 
                    request.getEmployee().getDepartment() == null ||
                    !manager.getDepartment().getId().equals(request.getEmployee().getDepartment().getId())) {
                throw new AccessDeniedException("Access denied: You can only approve leaves for your department employees.");
            }
        } else if (isApplicantManager) {
            // Must be approved by an Admin
            boolean isApproverAdmin = managerUser.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
            if (!isApproverAdmin) {
                throw new AccessDeniedException("Access denied: Manager leave requests can only be approved by the admin.");
            }
        } else {
            throw new BadRequestException("Admin leave requests are auto-approved and do not require manual review.");
        }

        request.setStatus(LeaveStatus.APPROVED);
        request.setApprovedBy(managerUser);
        LeaveRequest saved = leaveRequestRepository.save(request);

        populateAttendanceForApprovedLeave(saved);

        return leaveRequestMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public LeaveRequestDTO rejectLeave(Long id, String managerUsername) {
        User managerUser = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Manager user not found"));

        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already processed.");
        }

        User applicantUser = request.getEmployee().getUser();
        boolean isApplicantManager = applicantUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_MANAGER"));
        boolean isApplicantAdmin = applicantUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isPlainEmployee = !isApplicantManager && !isApplicantAdmin;

        if (isPlainEmployee) {
            boolean isApproverManager = managerUser.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_MANAGER"));
            if (!isApproverManager) {
                throw new AccessDeniedException("Access denied: Employee leave requests can only be rejected by their department manager, not by the admin.");
            }
            Employee manager = employeeRepository.findByUserId(managerUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager employee profile not found"));
            if (manager.getDepartment() == null ||
                    request.getEmployee().getDepartment() == null ||
                    !manager.getDepartment().getId().equals(request.getEmployee().getDepartment().getId())) {
                throw new AccessDeniedException("Access denied: You can only reject leaves for your department employees.");
            }
        } else if (isApplicantManager) {
            boolean isApproverAdmin = managerUser.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
            if (!isApproverAdmin) {
                throw new AccessDeniedException("Access denied: Manager leave requests can only be rejected by the admin.");
            }
        } else {
            throw new BadRequestException("Admin leave requests are auto-approved and cannot be rejected.");
        }

        request.setStatus(LeaveStatus.REJECTED);
        request.setApprovedBy(managerUser);
        LeaveRequest saved = leaveRequestRepository.save(request);

        return leaveRequestMapper.toDTO(saved);
    }

    private void populateAttendanceForApprovedLeave(LeaveRequest request) {
        LocalDate today = LocalDate.now();
        for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
            if (!date.isAfter(today)) {
                final LocalDate currentDate = date;
                Optional<Attendance> attOpt = attendanceRepository.findByEmployeeIdAndDate(request.getEmployee().getId(), currentDate);
                Attendance attendance = attOpt.orElseGet(() -> Attendance.builder()
                        .employee(request.getEmployee())
                        .date(currentDate)
                        .build());

                if (request.getLeaveType() == LeaveType.WFH) {
                    attendance.setWorkMode(WorkMode.REMOTE);
                    if (attendance.getStatus() == null || attendance.getStatus() == AttendanceStatus.ABSENT) {
                        attendance.setStatus(AttendanceStatus.WFH);
                    }
                } else {
                    attendance.setStatus(AttendanceStatus.ON_LEAVE);
                }
                attendanceRepository.save(attendance);
            }
        }
    }

    private Employee getEmployeeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user: " + username));
    }
}
