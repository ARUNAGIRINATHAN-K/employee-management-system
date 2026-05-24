package com.ems.controller;

import com.ems.entity.Employee;
import com.ems.security.UserPrincipal;
import com.ems.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Long departmentId = resolveDepartmentScope();
        Page<Employee> employees = employeeService.getAllEmployees(search, page, size, sortBy, sortDir, departmentId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            UserPrincipal principal = currentPrincipal();
            if ("ROLE_EMPLOYEE".equals(principal.getRole())) {
                Long employeeId = principal.getUser() != null && principal.getUser().getEmployee() != null
                        ? principal.getUser().getEmployee().getId()
                        : null;
                if (employeeId == null || !employeeId.equals(id)) {
                    return ResponseEntity.status(403).body(Map.of("message", "Employees may only view their own profile"));
                }
                return ResponseEntity.ok(employeeService.getEmployeeById(id));
            }

            Long departmentId = resolveDepartmentScope();
            Employee employee = employeeService.getEmployeeByIdScoped(id, departmentId);
            return ResponseEntity.ok(employee);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee) {
        try {
            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Employee created = employeeService.createEmployee(employee, adminUsername);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
        try {
            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Employee updated = employeeService.updateEmployee(id, employeeDetails, adminUsername);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            employeeService.deleteEmployee(id, adminUsername);
            return ResponseEntity.ok(Map.of("message", "Employee soft deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            UserPrincipal principal = currentPrincipal();
            String adminUsername = principal.getUsername();

            if ("ROLE_MANAGER".equals(principal.getRole())) {
                employeeService.getEmployeeByIdScoped(id, resolveDepartmentScope());
            }

            if ("ROLE_EMPLOYEE".equals(principal.getRole())) {
                Long employeeId = principal.getUser() != null && principal.getUser().getEmployee() != null
                        ? principal.getUser().getEmployee().getId()
                        : null;
                if (employeeId == null || !employeeId.equals(id)) {
                    return ResponseEntity.status(403).body(Map.of("message", "Employees may only update their own photo"));
                }
            }

            Employee updated = employeeService.uploadPhoto(id, file, adminUsername);
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to save file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<byte[]> exportToExcel() {
        try {
            byte[] data = employeeService.exportToExcel(resolveDepartmentScope());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasAnyAuthority('ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<byte[]> exportToPdf() {
        byte[] data = employeeService.exportToPdf(resolveDepartmentScope());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    private Long resolveDepartmentScope() {
        UserPrincipal principal = currentPrincipal();
        if ("ROLE_MANAGER".equals(principal.getRole())) {
            if (principal.getDepartmentId() == null) {
                throw new AccessDeniedException("Manager has no linked department");
            }
            return principal.getDepartmentId();
        }
        return null;
    }

    private UserPrincipal currentPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
