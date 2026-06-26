package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.dto.AttendancePolicyDTO;
import com.ems.employee_management_system.exception.ResourceNotFoundException;
import com.ems.employee_management_system.model.AttendancePolicy;
import com.ems.employee_management_system.repository.AttendancePolicyRepository;
import com.ems.employee_management_system.service.AttendancePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing shift timings and grace period configurations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendancePolicyServiceImpl implements AttendancePolicyService {

    private final AttendancePolicyRepository policyRepository;

    @Override
    public AttendancePolicyDTO getPolicy() {
        AttendancePolicy policy = policyRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Default attendance policy not found"));
        return mapToDTO(policy);
    }

    @Override
    @Transactional
    public AttendancePolicyDTO updatePolicy(AttendancePolicyDTO dto) {
        AttendancePolicy policy = policyRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Default attendance policy not found"));

        policy.setShiftStartTime(dto.getShiftStartTime());
        policy.setShiftEndTime(dto.getShiftEndTime());
        policy.setGracePeriodMinutes(dto.getGracePeriodMinutes());
        policy.setOvertimeThresholdMinutes(dto.getOvertimeThresholdMinutes());

        AttendancePolicy saved = policyRepository.save(policy);
        return mapToDTO(saved);
    }

    private AttendancePolicyDTO mapToDTO(AttendancePolicy policy) {
        return AttendancePolicyDTO.builder()
                .id(policy.getId())
                .shiftStartTime(policy.getShiftStartTime())
                .shiftEndTime(policy.getShiftEndTime())
                .gracePeriodMinutes(policy.getGracePeriodMinutes())
                .overtimeThresholdMinutes(policy.getOvertimeThresholdMinutes())
                .build();
    }
}
