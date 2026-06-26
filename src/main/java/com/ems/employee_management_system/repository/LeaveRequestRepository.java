package com.ems.employee_management_system.repository;

import com.ems.employee_management_system.model.LeaveRequest;
import com.ems.employee_management_system.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for LeaveRequest entity operations.
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    List<LeaveRequest> findByStatusOrderByStartDateDesc(LeaveStatus status);

    @Query("SELECT l FROM LeaveRequest l JOIN FETCH l.employee e WHERE e.department.id = :departmentId AND l.status = :status ORDER BY l.startDate DESC")
    List<LeaveRequest> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, @Param("status") LeaveStatus status);

    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.id = :employeeId AND l.status = 'APPROVED' AND :date BETWEEN l.startDate AND l.endDate")
    List<LeaveRequest> findApprovedLeaveByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
}
