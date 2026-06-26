package com.ems.employee_management_system.controller;

import com.ems.employee_management_system.dto.AttendancePolicyDTO;
import com.ems.employee_management_system.service.AttendancePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing company shift policies and timings.
 */
@RestController
@RequestMapping("/api/attendance-policy")
@RequiredArgsConstructor
public class AttendancePolicyController {

    private final AttendancePolicyService policyService;

    /**
     * GET /api/attendance-policy : Fetch company timings settings.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendancePolicyDTO> getPolicy() {
        return ResponseEntity.ok(policyService.getPolicy());
    }

    /**
     * PUT /api/attendance-policy : Update shift settings (Admin only).
     */
    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AttendancePolicyDTO> updatePolicy(@RequestBody AttendancePolicyDTO dto) {
        return ResponseEntity.ok(policyService.updatePolicy(dto));
    }
}
