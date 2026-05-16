package com.workforcehub.repository;

import com.workforcehub.entity.Attendance;
import com.workforcehub.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndAttendanceDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.status = :status AND a.deleted = false")
    long countByDateAndStatus(@Param("date") LocalDate date, @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.status IN ('PRESENT', 'LATE', 'WORK_FROM_HOME') AND a.deleted = false")
    long countPresentToday(@Param("date") LocalDate date);

    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.deleted = false GROUP BY a.status")
    List<Object[]> getAttendanceStatsByDate(@Param("date") LocalDate date);

    @Query("SELECT a.attendanceDate, COUNT(a) FROM Attendance a WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND a.status IN ('PRESENT', 'LATE', 'WORK_FROM_HOME') AND a.deleted = false GROUP BY a.attendanceDate ORDER BY a.attendanceDate")
    List<Object[]> getAttendanceTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.attendanceDate = CURRENT_DATE AND a.deleted = false")
    Optional<Attendance> findTodayAttendance(@Param("employeeId") Long employeeId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId AND a.late = true AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.deleted = false")
    long countLateMarks(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId AND a.status IN ('PRESENT', 'LATE', 'WORK_FROM_HOME') AND a.deleted = false")
    long countPresentByEmployee(@Param("employeeId") Long employeeId);
}
