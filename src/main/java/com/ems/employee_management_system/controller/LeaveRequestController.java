package com.ems.employee_management_system.controller;

import com.ems.employee_management_system.dto.LeaveRequestDTO;
import com.ems.employee_management_system.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST Controller for managing employee WFH and leave requests.
 */
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * POST /api/leaves : Submit a new leave or WFH request.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveRequestDTO> applyLeave(Principal principal, @RequestBody LeaveRequestDTO dto) {
        LeaveRequestDTO created = leaveRequestService.applyLeave(principal.getName(), dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * GET /api/leaves : Fetch the personal leave/WFH request history.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaveRequestDTO>> getPersonalLeaveHistory(Principal principal) {
        return ResponseEntity.ok(leaveRequestService.getPersonalLeaveHistory(principal.getName()));
    }

    /**
     * GET /api/leaves/pending : Fetch pending leave/WFH requests for approval (Manager only).
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<List<LeaveRequestDTO>> getPendingLeaveRequests(Principal principal) {
        return ResponseEntity.ok(leaveRequestService.getPendingLeaveRequests(principal.getName()));
    }

    /**
     * PUT /api/leaves/{id}/approve : Approve a pending request.
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<LeaveRequestDTO> approveLeave(@PathVariable("id") Long id, Principal principal) {
        return ResponseEntity.ok(leaveRequestService.approveLeave(id, principal.getName()));
    }

    /**
     * PUT /api/leaves/{id}/reject : Reject a pending request.
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<LeaveRequestDTO> rejectLeave(@PathVariable("id") Long id, Principal principal) {
        return ResponseEntity.ok(leaveRequestService.rejectLeave(id, principal.getName()));
    }

    /**
     * GET /api/leaves/history : Fetch full team/global leave requests history (Admin and Manager only).
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<List<LeaveRequestDTO>> getTeamLeaveHistory(Principal principal) {
        return ResponseEntity.ok(leaveRequestService.getTeamLeaveHistory(principal.getName()));
    }
}
