package com.ems.employee_management_system.controller;

import com.ems.employee_management_system.dto.EmployeeDTO;
import com.ems.employee_management_system.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Employees.
 *
 * <p><b>RBAC Permission Matrix:</b></p>
 * <ul>
 *   <li>POST / PUT / DELETE – ADMIN, HR</li>
 *   <li>GET ALL             – ADMIN, HR, MANAGER</li>
 *   <li>GET BY ID           – ADMIN, HR, MANAGER, or the owning EMPLOYEE (via SpEL)</li>
 *   <li>GET BY USER ID      – ADMIN, HR, MANAGER, or self (via SpEL)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ──────────────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────────────

    /**
     * POST /api/employees
     * Allowed: ADMIN, HR
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO created = employeeService.createEmployee(employeeDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // ──────────────────────────────────────────────────────────────────────
    // READ – paginated list
    // ──────────────────────────────────────────────────────────────────────

    /**
     * GET /api/employees
     * Allowed: ADMIN, HR, MANAGER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "status", required = false) String status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(search, departmentId, status, pageable));
    }

    // ──────────────────────────────────────────────────────────────────────
    // READ – single record
    // ──────────────────────────────────────────────────────────────────────

    /**
     * GET /api/employees/{id}
     * Allowed: ADMIN, HR, MANAGER, or the EMPLOYEE who owns this record.
     * Uses the custom SpEL bean {@code @sec.isOwnerOrPrivileged(#id)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@sec.isOwnerOrPrivileged(#id)")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /**
     * GET /api/employees/user/{userId}
     * Allowed: ADMIN, HR, MANAGER, or the USER who owns this profile.
     * Uses the custom SpEL bean {@code @sec.isSelfOrPrivileged(#userId)}.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("@sec.isSelfOrPrivileged(#userId)")
    public ResponseEntity<EmployeeDTO> getEmployeeByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(employeeService.getEmployeeByUserId(userId));
    }

    // ──────────────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────────────

    /**
     * PUT /api/employees/{id}
     * Allowed: ADMIN, HR
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable("id") Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDTO));
    }

    // ──────────────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────────────

    /**
     * DELETE /api/employees/{id}
     * Allowed: ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
