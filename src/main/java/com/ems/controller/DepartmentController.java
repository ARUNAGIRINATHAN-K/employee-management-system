package com.ems.controller;

import com.ems.entity.Department;
import com.ems.entity.Employee;
import com.ems.repository.EmployeeRepository;
import com.ems.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_HR','ROLE_ADMIN')")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR','ROLE_ADMIN')")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping("/{id}/employees")
    @PreAuthorize("hasAnyAuthority('ROLE_HR','ROLE_ADMIN')")
    public ResponseEntity<List<Employee>> getDepartmentEmployees(@PathVariable Long id) {
        return ResponseEntity.ok(employeeRepository.findByDepartmentIdAndStatusNot(id, "DELETED"));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_HR','ROLE_ADMIN')")
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        try {
            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Department created = departmentService.createDepartment(department, adminUsername);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR','ROLE_ADMIN')")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody Department departmentDetails) {
        try {
            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Department updated = departmentService.updateDepartment(id, departmentDetails, adminUsername);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR','ROLE_ADMIN')")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            departmentService.deleteDepartment(id, adminUsername);
            return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('ROLE_HR','ROLE_ADMIN')")
    public ResponseEntity<?> assignEmployees(@PathVariable Long id, @RequestBody List<Long> employeeIds) {
        try {
            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            departmentService.assignEmployeesToDepartment(id, employeeIds, adminUsername);
            return ResponseEntity.ok(Map.of("message", "Employees assigned to department successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
