package com.ems.repository;

import com.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByStatusNot(String status, Pageable pageable);

    List<Employee> findByStatusNot(String status);

    @Query("SELECT e FROM Employee e WHERE e.status <> 'DELETED' AND " +
           "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.jobTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.department.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);

    List<Employee> findByDepartmentIdAndStatusNot(Long departmentId, String status);

    List<Employee> findByManagerIdAndStatusNot(Long managerId, String status);
    
    long countByStatus(String status);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status <> 'DELETED'")
    long countActiveEmployees();

    @Query("SELECT e.department.name, COUNT(e) FROM Employee e WHERE e.status <> 'DELETED' GROUP BY e.department.name")
    List<Object[]> countEmployeesByDepartment();
}
