package com.ems.employee_management_system.mapper;

import com.ems.employee_management_system.dto.AttendanceDTO;
import com.ems.employee_management_system.model.Attendance;
import com.ems.employee_management_system.model.AttendanceStatus;
import com.ems.employee_management_system.model.WorkMode;
import org.springframework.stereotype.Component;

/**
 * Mapper utility class to convert between Attendance Entity and AttendanceDTO.
 */
@Component
public class AttendanceMapper {

    public AttendanceDTO toDTO(Attendance attendance) {
        if (attendance == null) {
            return null;
        }

        AttendanceDTO.AttendanceDTOBuilder builder = AttendanceDTO.builder()
                .id(attendance.getId())
                .date(attendance.getDate())
                .clockIn(attendance.getClockIn())
                .clockOut(attendance.getClockOut())
                .status(attendance.getStatus() != null ? attendance.getStatus().name() : null)
                .workMode(attendance.getWorkMode() != null ? attendance.getWorkMode().name() : null)
                .overtimeMinutes(attendance.getOvertimeMinutes())
                .lateMinutes(attendance.getLateMinutes());

        if (attendance.getEmployee() != null) {
            builder.employeeId(attendance.getEmployee().getId());
            builder.employeeName(attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName());
        }

        return builder.build();
    }

    public Attendance toEntity(AttendanceDTO dto) {
        if (dto == null) {
            return null;
        }

        Attendance.AttendanceBuilder builder = Attendance.builder()
                .id(dto.getId())
                .date(dto.getDate())
                .clockIn(dto.getClockIn())
                .clockOut(dto.getClockOut())
                .overtimeMinutes(dto.getOvertimeMinutes())
                .lateMinutes(dto.getLateMinutes());

        if (dto.getStatus() != null) {
            try {
                builder.status(AttendanceStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                builder.status(AttendanceStatus.ABSENT);
            }
        }

        if (dto.getWorkMode() != null) {
            try {
                builder.workMode(WorkMode.valueOf(dto.getWorkMode().toUpperCase()));
            } catch (IllegalArgumentException e) {
                builder.workMode(WorkMode.OFFICE);
            }
        }

        return builder.build();
    }
}
