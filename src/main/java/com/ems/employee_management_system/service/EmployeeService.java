package com.ems.employee_management_system.service;

import com.ems.employee_management_system.dto.AssignAccountRequest;
import com.ems.employee_management_system.dto.EmployeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Employee operations.
 */
public interface EmployeeService {

    /**
     * Retrieves all employees matching search criteria (by name/email), department, and status, with pagination/sorting.
     */
    Page<EmployeeDTO> getAllEmployees(String search, Long departmentId, String status, Pageable pageable);

    /**
     * Retrieves an employee by ID.
     */
    EmployeeDTO getEmployeeById(Long id);

    /**
     * Retrieves an employee by linked user ID.
     */
    EmployeeDTO getEmployeeByUserId(Long userId);

    /**
     * Creates a new employee.
     */
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);

    /**
     * Updates an existing employee.
     */
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);

    /**
     * Deletes an employee.
     */
    void deleteEmployee(Long id);

    /**
     * Creates a new User account with the given credentials and role,
     * then links it to the employee identified by {@code employeeId}.
     * Throws if the employee already has a linked account.
     */
    EmployeeDTO assignAccount(Long employeeId, AssignAccountRequest request);
}

