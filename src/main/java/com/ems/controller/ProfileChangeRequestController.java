package com.ems.controller;

import com.ems.entity.Employee;
import com.ems.entity.ProfileChangeRequest;
import com.ems.entity.User;
import com.ems.repository.UserRepository;
import com.ems.service.ProfileChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile-changes")
public class ProfileChangeRequestController {

    @Autowired
    private ProfileChangeRequestService profileChangeRequestService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Employee employee = user.getEmployee();
            if (employee == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Logged in user has no employee profile"));
            }
            List<ProfileChangeRequest> requests = profileChangeRequestService.getRequestsByEmployee(employee.getId());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<List<ProfileChangeRequest>> getPendingRequests() {
        return ResponseEntity.ok(profileChangeRequestService.getPendingRequests());
    }

    @PostMapping
    public ResponseEntity<?> submitRequest(@RequestBody Map<String, String> payload) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Employee employee = user.getEmployee();
            if (employee == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Logged in user has no employee profile"));
            }
            
            String requestedFieldsJson = payload.get("requestedFieldsJson");
            if (requestedFieldsJson == null || requestedFieldsJson.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "requestedFieldsJson is required"));
            }

            ProfileChangeRequest request = profileChangeRequestService.submitRequest(
                    employee.getId(), requestedFieldsJson, username);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<?> approveRequest(@PathVariable Long id) {
        try {
            String hrUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            ProfileChangeRequest request = profileChangeRequestService.approveRequest(id, hrUsername);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_HR')")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String hrUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            String comments = payload.getOrDefault("comments", "Rejected by HR");
            ProfileChangeRequest request = profileChangeRequestService.rejectRequest(id, comments, hrUsername);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
