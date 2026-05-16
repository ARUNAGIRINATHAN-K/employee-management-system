package com.workforcehub.repository;

import com.workforcehub.entity.LeaveRequest;
import com.workforcehub.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = :status AND lr.deleted = false")
    long countByStatus(@Param("status") LeaveStatus status);

    @Query("SELECT lr.leaveType, COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = 'APPROVED' AND lr.deleted = false GROUP BY lr.leaveType")
    List<Object[]> getLeaveStatsByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' AND lr.deleted = false ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findPendingRequests();

    @Query("SELECT lr.leaveType, SUM(lr.totalDays) FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND lr.deleted = false GROUP BY lr.leaveType")
    List<Object[]> getLeaveDistribution();

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = 'PENDING' AND lr.deleted = false")
    long countPendingByEmployee(@Param("employeeId") Long employeeId);
}
