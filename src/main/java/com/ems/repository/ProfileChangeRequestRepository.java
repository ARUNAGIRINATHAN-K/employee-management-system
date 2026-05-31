package com.ems.repository;

import com.ems.entity.ProfileChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface ProfileChangeRequestRepository extends JpaRepository<ProfileChangeRequest, Long> {
    List<ProfileChangeRequest> findByEmployeeId(Long employeeId);
    List<ProfileChangeRequest> findByStatus(String status);

    @Query("SELECT p FROM ProfileChangeRequest p WHERE p.employee.department.id = :deptId")
    List<ProfileChangeRequest> findByEmployeeDepartmentId(@Param("deptId") Long deptId);
}
