package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.dto.EmployeeDTO;
import com.ems.employee_management_system.exception.DuplicateResourceException;
import com.ems.employee_management_system.exception.ResourceNotFoundException;
import com.ems.employee_management_system.mapper.EmployeeMapper;
import com.ems.employee_management_system.model.Department;
import com.ems.employee_management_system.model.Employee;
import com.ems.employee_management_system.model.EmployeeStatus;
import com.ems.employee_management_system.model.User;
import com.ems.employee_management_system.repository.DepartmentRepository;
import com.ems.employee_management_system.repository.EmployeeRepository;
import com.ems.employee_management_system.repository.UserRepository;
import com.ems.employee_management_system.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Employee operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public Page<EmployeeDTO> getAllEmployees(String search, Long departmentId, String status, Pageable pageable) {
        EmployeeStatus employeeStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                employeeStatus = EmployeeStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status input and keep it null
            }
        }
        Page<Employee> employees = employeeRepository.searchEmployees(search, departmentId, employeeStatus, pageable);
        return employees.map(employeeMapper::toDTO);
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return employeeMapper.toDTO(employee);
    }

    @Override
    public EmployeeDTO getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user id: " + userId));
        return employeeMapper.toDTO(employee);
    }

    @Override
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        // Validation: Email must be unique
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + employeeDTO.getEmail() + "' already exists");
        }

        // Validation: Verify department exists
        Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + employeeDTO.getDepartmentId()));

        Employee employee = employeeMapper.toEntity(employeeDTO);
        employee.setDepartment(department);

        // Optional Validation & Association: Link to User account
        if (employeeDTO.getUserId() != null) {
            User user = userRepository.findById(employeeDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + employeeDTO.getUserId()));
            
            // Check if user is already linked to another employee
            if (employeeRepository.findByUserId(employeeDTO.getUserId()).isPresent()) {
                throw new DuplicateResourceException("User with id '" + employeeDTO.getUserId() + "' is already linked to another employee profile");
            }
            employee.setUser(user);
        }

        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDTO(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Validation: Email check if changed
        if (!employee.getEmail().equalsIgnoreCase(employeeDTO.getEmail()) &&
                employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Employee with email '" + employeeDTO.getEmail() + "' already exists");
        }

        // Validation: Verify department exists
        Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + employeeDTO.getDepartmentId()));

        employeeMapper.updateEntityFromDTO(employeeDTO, employee);
        employee.setDepartment(department);

        // Optional Association: Link/Update User account connection
        if (employeeDTO.getUserId() != null) {
            if (employee.getUser() == null || !employee.getUser().getId().equals(employeeDTO.getUserId())) {
                User user = userRepository.findById(employeeDTO.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + employeeDTO.getUserId()));
                
                employeeRepository.findByUserId(employeeDTO.getUserId())
                        .filter(existingEmp -> !existingEmp.getId().equals(employee.getId()))
                        .ifPresent(existingEmp -> {
                            throw new DuplicateResourceException("User with id '" + employeeDTO.getUserId() + "' is already linked to another employee profile");
                        });
                
                employee.setUser(user);
            }
        } else {
            employee.setUser(null);
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDTO(updatedEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employeeRepository.delete(employee);
    }
}
