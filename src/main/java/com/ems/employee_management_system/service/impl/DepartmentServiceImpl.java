package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.dto.DepartmentDTO;
import com.ems.employee_management_system.exception.BadRequestException;
import com.ems.employee_management_system.exception.DuplicateResourceException;
import com.ems.employee_management_system.exception.ResourceNotFoundException;
import com.ems.employee_management_system.mapper.DepartmentMapper;
import com.ems.employee_management_system.model.Department;
import com.ems.employee_management_system.model.Employee;
import com.ems.employee_management_system.repository.DepartmentRepository;
import com.ems.employee_management_system.repository.EmployeeRepository;
import com.ems.employee_management_system.repository.UserRepository;
import com.ems.employee_management_system.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Department operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public Page<DepartmentDTO> getAllDepartments(String search, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            boolean isOnlyManager = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")) &&
                    auth.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
            if (isOnlyManager) {
                Optional<Department> managerDeptOpt = userRepository.findByUsername(auth.getName())
                        .flatMap(user -> employeeRepository.findByUserId(user.getId()))
                        .map(Employee::getDepartment);
                if (managerDeptOpt.isPresent()) {
                    Department dept = managerDeptOpt.get();
                    if (search == null || search.trim().isEmpty() ||
                            dept.getName().toLowerCase().contains(search.toLowerCase())) {
                        List<DepartmentDTO> list = List.of(departmentMapper.toDTO(dept));
                        return new PageImpl<>(list, pageable, 1);
                    }
                }
                return new PageImpl<>(new ArrayList<>(), pageable, 0);
            }
        }
        Page<Department> departments = departmentRepository.searchDepartments(search, pageable);
        return departments.map(departmentMapper::toDTO);
    }

    @Override
    public List<DepartmentDTO> getAllDepartmentsList() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            boolean isOnlyManager = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")) &&
                    auth.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
            if (isOnlyManager) {
                Optional<Department> managerDeptOpt = userRepository.findByUsername(auth.getName())
                        .flatMap(user -> employeeRepository.findByUserId(user.getId()))
                        .map(Employee::getDepartment);
                if (managerDeptOpt.isPresent()) {
                    return List.of(departmentMapper.toDTO(managerDeptOpt.get()));
                }
                return new ArrayList<>();
            }
        }
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                .map(departmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDTO getDepartmentById(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            boolean isOnlyManager = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")) &&
                    auth.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
            if (isOnlyManager) {
                Long managerDeptId = userRepository.findByUsername(auth.getName())
                        .flatMap(user -> employeeRepository.findByUserId(user.getId()))
                        .map(Employee::getDepartment)
                        .map(Department::getId)
                        .orElse(-1L);
                if (!id.equals(managerDeptId)) {
                    throw new org.springframework.security.access.AccessDeniedException("Access denied: Managers can only view their own department");
                }
            }
        }
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
