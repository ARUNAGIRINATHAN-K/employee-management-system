package com.ems.employee_management_system.mapper;

import com.ems.employee_management_system.dto.EmployeeDTO;
import com.ems.employee_management_system.model.Employee;
import com.ems.employee_management_system.model.EmployeeStatus;
import org.springframework.stereotype.Component;

/**
 * Mapper utility class to convert between Employee Entity and EmployeeDTO manually.
 */
@Component
public class EmployeeMapper {

    /**
     * Map Employee Entity to EmployeeDTO.
     */
    public EmployeeDTO toDTO(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeDTO.EmployeeDTOBuilder builder = EmployeeDTO.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .jobTitle(employee.getJobTitle())
                .salary(employee.getSalary())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus() != null ? employee.getStatus().name() : null)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt());

        // Handle Department relation mapping
        if (employee.getDepartment() != null) {
            builder.departmentId(employee.getDepartment().getId());
            builder.departmentName(employee.getDepartment().getName());
        }

        // Handle User relation mapping
        if (employee.getUser() != null) {
            builder.userId(employee.getUser().getId());
            builder.username(employee.getUser().getUsername());
        }

        return builder.build();
    }

    /**
     * Map EmployeeDTO to Employee Entity.
     * Note: Relations (Department, User) should be resolved and set in the Service layer.
     */
    public Employee toEntity(EmployeeDTO dto) {
        if (dto == null) {
            return null;
        }

        Employee.EmployeeBuilder builder = Employee.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .jobTitle(dto.getJobTitle())
                .salary(dto.getSalary())
                .hireDate(dto.getHireDate());

        if (dto.getStatus() != null) {
            try {
                builder.status(EmployeeStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Default fallback if invalid status string is provided
                builder.status(EmployeeStatus.ACTIVE);
            }
        }

        return builder.build();
    }

    /**
     * Update existing Employee Entity with fields from EmployeeDTO.
     * Note: Relations (Department, User) should be updated in the Service layer.
     */
    public void updateEntityFromDTO(EmployeeDTO dto, Employee employee) {
        if (dto == null || employee == null) {
            return;
        }

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setJobTitle(dto.getJobTitle());
        employee.setSalary(dto.getSalary());
        employee.setHireDate(dto.getHireDate());

        if (dto.getStatus() != null) {
            try {
                employee.setStatus(EmployeeStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep existing status if invalid status string is passed
            }
        }
    }
}
