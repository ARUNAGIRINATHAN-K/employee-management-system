package com.ems.employee_management_system.service;

import com.ems.employee_management_system.dto.DepartmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service interface for Department operations.
 */
public interface DepartmentService {
    
    /**
     * Retrieves a page of departments matching search criteria, with pagination and sorting.
     */
    Page<DepartmentDTO> getAllDepartments(String search, Pageable pageable);
    
    /**
     * Retrieves all departments as a list (useful for dropdown menus).
     */
    List<DepartmentDTO> getAllDepartmentsList();
    
    /**
     * Retrieves a department by ID.
     */
    DepartmentDTO getDepartmentById(Long id);
    
    /**
     * Creates a new department.
     */
    DepartmentDTO createDepartment(DepartmentDTO departmentDTO);
    
    /**
     * Updates an existing department.
     */
    DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO);
    
    /**
     * Deletes a department.
     */
    void deleteDepartment(Long id);
}
