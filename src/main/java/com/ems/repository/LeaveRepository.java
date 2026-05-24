package com.ems.repository;

import com.ems.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);
    List<Leave> findByStatus(String status);
    
    @Query("SELECT l FROM Leave l WHERE l.employee.manager.id = :managerId")
    List<Leave> findByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT l FROM Leave l WHERE l.employee.department.id = :deptId")
    List<Leave> findByEmployeeDepartmentId(@Param("deptId") Long deptId);

    @Query("SELECT l.leaveType, COUNT(l) FROM Leave l GROUP BY l.leaveType")
    List<Object[]> countLeavesByType();
}
