package com.ems.employee_management_system.mapper;

import com.ems.employee_management_system.dto.LeaveRequestDTO;
import com.ems.employee_management_system.model.LeaveRequest;
import com.ems.employee_management_system.model.LeaveStatus;
import com.ems.employee_management_system.model.LeaveType;
import org.springframework.stereotype.Component;

/**
 * Mapper utility class to convert between LeaveRequest Entity and LeaveRequestDTO.
 */
@Component
public class LeaveRequestMapper {

    public LeaveRequestDTO toDTO(LeaveRequest request) {
        if (request == null) {
            return null;
        }

        LeaveRequestDTO.LeaveRequestDTOBuilder builder = LeaveRequestDTO.builder()
                .id(request.getId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .leaveType(request.getLeaveType() != null ? request.getLeaveType().name() : null);

        if (request.getEmployee() != null) {
            builder.employeeId(request.getEmployee().getId());
            builder.employeeName(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName());
        }

        if (request.getApprovedBy() != null) {
            builder.approvedByUsername(request.getApprovedBy().getUsername());
        }

        return builder.build();
    }

    public LeaveRequest toEntity(LeaveRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        LeaveRequest.LeaveRequestBuilder builder = LeaveRequest.builder()
                .id(dto.getId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason());

        if (dto.getStatus() != null) {
            try {
                builder.status(LeaveStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                builder.status(LeaveStatus.PENDING);
            }
        }

        if (dto.getLeaveType() != null) {
            try {
                builder.leaveType(LeaveType.valueOf(dto.getLeaveType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                builder.leaveType(LeaveType.CASUAL);
            }
        }

        return builder.build();
    }
}
