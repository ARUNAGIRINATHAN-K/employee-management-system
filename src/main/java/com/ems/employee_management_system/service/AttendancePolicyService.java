package com.ems.employee_management_system.service;

import com.ems.employee_management_system.dto.AttendancePolicyDTO;

/**
 * Service interface for managing shift timings and grace period configurations.
 */
public interface AttendancePolicyService {
    AttendancePolicyDTO getPolicy();
    AttendancePolicyDTO updatePolicy(AttendancePolicyDTO dto);
}
