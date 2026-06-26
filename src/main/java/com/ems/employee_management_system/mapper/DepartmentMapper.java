package com.ems.employee_management_system.mapper;

import com.ems.employee_management_system.dto.DepartmentDTO;
import com.ems.employee_management_system.model.Department;
import org.springframework.stereotype.Component;

/**
 * Mapper utility class to convert between Department Entity and DepartmentDTO manually.
 */
@Component
public class DepartmentMapper {

    /**
     * Map Department Entity to DepartmentDTO.
     */
    public DepartmentDTO toDTO(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentDTO dto = DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();

        // If the collection of employees is initialized, we can map employee count.
        if (department.getEmployees() != null) {
            dto.setEmployeeCount(department.getEmployees().size());
        }

        return dto;
    }

    /**
     * Map DepartmentDTO to Department Entity.
     */
    public Department toEntity(DepartmentDTO dto) {
        if (dto == null) {
            return null;
        }

        return Department.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    /**
     * Update existing Department Entity with fields from DepartmentDTO.
     */
    public void updateEntityFromDTO(DepartmentDTO dto, Department department) {
        if (dto == null || department == null) {
            return;
        }
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
    }
}
