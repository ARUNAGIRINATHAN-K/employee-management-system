package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.dto.DepartmentDTO;
import com.ems.employee_management_system.exception.BadRequestException;
import com.ems.employee_management_system.exception.DuplicateResourceException;
import com.ems.employee_management_system.exception.ResourceNotFoundException;
import com.ems.employee_management_system.mapper.DepartmentMapper;
import com.ems.employee_management_system.model.Department;
import com.ems.employee_management_system.repository.DepartmentRepository;
import com.ems.employee_management_system.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Department operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public Page<DepartmentDTO> getAllDepartments(String search, Pageable pageable) {
        Page<Department> departments = departmentRepository.searchDepartments(search, pageable);
        return departments.map(departmentMapper::toDTO);
    }

    @Override
    public List<DepartmentDTO> getAllDepartmentsList() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                .map(departmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return departmentMapper.toDTO(department);
    }

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        if (departmentRepository.existsByName(departmentDTO.getName())) {
            throw new DuplicateResourceException("Department with name '" + departmentDTO.getName() + "' already exists");
        }

        Department department = departmentMapper.toEntity(departmentDTO);
        Department savedDepartment = departmentRepository.save(department);
        return departmentMapper.toDTO(savedDepartment);
    }

    @Override
    @Transactional
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Check if name is updated and already exists for another department
        if (!department.getName().equalsIgnoreCase(departmentDTO.getName()) &&
                departmentRepository.existsByName(departmentDTO.getName())) {
            throw new DuplicateResourceException("Department with name '" + departmentDTO.getName() + "' already exists");
        }

        departmentMapper.updateEntityFromDTO(departmentDTO, department);
        Department updatedDepartment = departmentRepository.save(department);
        return departmentMapper.toDTO(updatedDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Business Rule: Restrict deleting a department if it contains active employees
        if (department.getEmployees() != null && !department.getEmployees().isEmpty()) {
            boolean hasEmployees = department.getEmployees().stream()
                    .anyMatch(emp -> emp.getStatus() == com.ems.employee_management_system.model.EmployeeStatus.ACTIVE);
            if (hasEmployees) {
                throw new BadRequestException("Cannot delete department with active employees. Please reassign employees first.");
            }
        }

        departmentRepository.delete(department);
    }
}
