package com.ems.employee_management_system.service;

import com.ems.employee_management_system.dto.DepartmentDTO;
import com.ems.employee_management_system.exception.BadRequestException;
import com.ems.employee_management_system.exception.DuplicateResourceException;
import com.ems.employee_management_system.mapper.DepartmentMapper;
import com.ems.employee_management_system.model.Department;
import com.ems.employee_management_system.model.Employee;
import com.ems.employee_management_system.model.EmployeeStatus;
import com.ems.employee_management_system.repository.DepartmentRepository;
import com.ems.employee_management_system.repository.EmployeeRepository;
import com.ems.employee_management_system.repository.UserRepository;
import com.ems.employee_management_system.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    private final DepartmentMapper departmentMapper = new DepartmentMapper();

    private DepartmentServiceImpl departmentService;

    private Department department;
    private DepartmentDTO departmentDTO;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentServiceImpl(
                departmentRepository,
                employeeRepository,
                userRepository,
                departmentMapper
        );


        department = new Department();
        department.setId(1L);
        department.setName("Engineering");
        department.setDescription("Core tech development");

        departmentDTO = DepartmentDTO.builder()
                .id(1L)
                .name("Engineering")
                .description("Core tech development")
                .build();
    }

    @Test
    void getDepartmentById_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        DepartmentDTO found = departmentService.getDepartmentById(1L);

        assertNotNull(found);
        assertEquals("Engineering", found.getName());
        assertEquals("Core tech development", found.getDescription());
    }

    @Test
    void createDepartment_DuplicateName_ThrowsException() {
        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> departmentService.createDepartment(departmentDTO));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void deleteDepartment_WithActiveEmployees_ThrowsException() {
        Employee employee = new Employee();
        employee.setStatus(EmployeeStatus.ACTIVE);
        department.setEmployees(List.of(employee));

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        assertThrows(BadRequestException.class, () -> departmentService.deleteDepartment(1L));
        verify(departmentRepository, never()).delete(any());
    }
}
