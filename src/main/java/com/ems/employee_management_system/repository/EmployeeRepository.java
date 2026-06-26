package com.ems.employee_management_system.repository;

import com.ems.employee_management_system.model.Employee;
import com.ems.employee_management_system.model.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity operations.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmail(String email);
    
    Boolean existsByEmail(String email);

    Optional<Employee> findByUserId(Long userId);

    /**
     * Custom JPQL query to search, filter, paginate, and sort employees.
     */
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.department d " +
           "LEFT JOIN FETCH e.user u " +
           "WHERE (:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Employee> searchEmployees(
            @Param("search") String search, 
            @Param("departmentId") Long departmentId, 
            @Param("status") EmployeeStatus status, 
            Pageable pageable);

    /**
     * Custom analytical query to compute the average salary of active employees.
     */
    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.status = 'ACTIVE'")
    BigDecimal getAverageSalaryOfActiveEmployees();

    /**
     * Custom query to count active employees.
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = 'ACTIVE'")
    long countByStatusActive();

    /**
     * Custom aggregation query to get active employee count grouped by department.
     */
    @Query("SELECT e.department.name, COUNT(e) FROM Employee e " +
           "WHERE e.status = 'ACTIVE' AND e.department IS NOT NULL " +
           "GROUP BY e.department.name")
    List<Object[]> getActiveEmployeeCountByDepartment();

    /**
     * Derived query to count employees by department.
     */
    long countByDepartmentId(Long departmentId);

    /**
     * Derived query to count employees by department and status.
     */
    long countByDepartmentIdAndStatus(Long departmentId, EmployeeStatus status);

    /**
     * Custom analytical query to compute the average salary of active employees in a specific department.
     */
    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.status = 'ACTIVE' AND e.department.id = :departmentId")
    BigDecimal getAverageSalaryOfActiveEmployeesByDepartment(@Param("departmentId") Long departmentId);

    /**
     * Custom aggregation query to get active employee count grouped by job title for a department.
     */
    @Query("SELECT e.jobTitle, COUNT(e) FROM Employee e " +
           "WHERE e.status = 'ACTIVE' AND e.department.id = :departmentId " +
           "GROUP BY e.jobTitle")
    List<Object[]> getActiveEmployeeCountByJobTitleForDepartment(@Param("departmentId") Long departmentId);
}

