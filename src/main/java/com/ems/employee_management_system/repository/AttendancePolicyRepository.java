package com.ems.employee_management_system.repository;

import com.ems.employee_management_system.model.AttendancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for AttendancePolicy entity operations.
 */
@Repository
public interface AttendancePolicyRepository extends JpaRepository<AttendancePolicy, Long> {
}
