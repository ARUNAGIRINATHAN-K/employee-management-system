package com.ems.service;

import com.ems.entity.Employee;
import com.ems.entity.ProfileChangeRequest;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.ProfileChangeRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ProfileChangeRequestService {

    @Autowired
    private ProfileChangeRequestRepository profileChangeRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ProfileChangeRequest> getRequestsByEmployee(Long employeeId) {
        return profileChangeRequestRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<ProfileChangeRequest> getPendingRequests() {
        return profileChangeRequestRepository.findByStatus("PENDING");
    }

    @Transactional
    public ProfileChangeRequest submitRequest(Long employeeId, String requestedFieldsJson, String requesterUsername) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Validate JSON parses
        try {
            objectMapper.readValue(requestedFieldsJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid requested fields format: " + e.getMessage());
        }

        ProfileChangeRequest request = ProfileChangeRequest.builder()
                .employee(employee)
                .requestedFieldsJson(requestedFieldsJson)
                .status("PENDING")
                .submittedAt(LocalDateTime.now())
                .build();

        ProfileChangeRequest saved = profileChangeRequestRepository.save(request);
        auditLogService.log("PROFILE_CHANGE_SUBMIT", requesterUsername,
                "Submitted a profile change request (ID: " + saved.getId() + ")");
        notificationService.sendNotification("New profile change request submitted by " + employee.getFirstName() + " " + employee.getLastName());
        return saved;
    }

    @Transactional
    public ProfileChangeRequest approveRequest(Long requestId, String hrUsername) {
        ProfileChangeRequest request = profileChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Profile change request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request has already been processed.");
        }

        Employee hrEmployee = employeeRepository.findByStatusNot("DELETED").stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(hrUsername))
                .findFirst().orElse(null);

        Employee employee = request.getEmployee();

        try {
            @SuppressWarnings("unchecked")
            Map<String, String> fields = objectMapper.readValue(request.getRequestedFieldsJson(), Map.class);
            if (fields.containsKey("firstName") && fields.get("firstName") != null) {
                employee.setFirstName(fields.get("firstName"));
            }
            if (fields.containsKey("lastName") && fields.get("lastName") != null) {
                employee.setLastName(fields.get("lastName"));
            }
            if (fields.containsKey("phone") && fields.get("phone") != null) {
                employee.setPhone(fields.get("phone"));
            }
            employeeRepository.save(employee);
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply profile changes: " + e.getMessage());
        }

        request.setStatus("APPROVED");
        request.setProcessedBy(hrEmployee);
        request.setProcessedAt(LocalDateTime.now());
        ProfileChangeRequest saved = profileChangeRequestRepository.save(request);

        auditLogService.log("PROFILE_CHANGE_APPROVE", hrUsername,
                "Approved profile change request ID: " + requestId + " for employee ID: " + employee.getId());
        notificationService.sendNotification("Profile change request for " + employee.getFirstName() + " " + employee.getLastName() + " has been APPROVED.");
        return saved;
    }

    @Transactional
    public ProfileChangeRequest rejectRequest(Long requestId, String comments, String hrUsername) {
        ProfileChangeRequest request = profileChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Profile change request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request has already been processed.");
        }

        Employee hrEmployee = employeeRepository.findByStatusNot("DELETED").stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(hrUsername))
                .findFirst().orElse(null);

        request.setStatus("REJECTED");
        request.setProcessedBy(hrEmployee);
        request.setProcessedAt(LocalDateTime.now());
        request.setComments(comments);
        ProfileChangeRequest saved = profileChangeRequestRepository.save(request);

        auditLogService.log("PROFILE_CHANGE_REJECT", hrUsername,
                "Rejected profile change request ID: " + requestId + " with comments: " + comments);
        notificationService.sendNotification("Profile change request for " + request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName() + " has been REJECTED.");
        return saved;
    }
}
