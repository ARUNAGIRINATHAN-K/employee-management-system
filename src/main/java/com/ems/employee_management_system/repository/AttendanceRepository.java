package com.ems.employee_management_system.repository;

import com.ems.employee_management_system.model.Attendance;
import com.ems.employee_management_system.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Attendance entity operations.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateDesc(Long employeeId, LocalDate startDate, LocalDate endDate);

    List<Attendance> findByDate(LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE e.department.id = :departmentId AND a.date = :date")
    List<Attendance> findByDepartmentIdAndDate(@Param("departmentId") Long departmentId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE e.department.id = :departmentId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date DESC")
    List<Attendance> findByDepartmentIdAndDateBetween(@Param("departmentId") Long departmentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a.status, COUNT(a) FROM Attendance a JOIN a.employee e WHERE e.department.id = :departmentId AND a.date = :date GROUP BY a.status")
    List<Object[]> countStatusByDepartmentIdAndDate(@Param("departmentId") Long departmentId, @Param("date") LocalDate date);

    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.date = :date GROUP BY a.status")
    List<Object[]> countStatusByDate(@Param("date") LocalDate date);
}
