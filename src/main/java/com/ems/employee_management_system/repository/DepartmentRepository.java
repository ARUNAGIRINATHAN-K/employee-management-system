package com.ems.employee_management_system.repository;

import com.ems.employee_management_system.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Department entity operations.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Boolean existsByName(String name);

    /**
     * Custom JPQL query to search departments by name with pagination and sorting.
     */
    @Query("SELECT d FROM Department d WHERE " +
           "(:search IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Department> searchDepartments(@Param("search") String search, Pageable pageable);

    /**
     * Custom JPQL query to fetch departments along with their active employee count.
     * Returns an array of objects [Department, Long count].
     */
    @Query("SELECT d, COUNT(e) FROM Department d " +
           "LEFT JOIN d.employees e ON e.status = 'ACTIVE' " +
           "GROUP BY d")
    List<Object[]> getDepartmentsWithEmployeeCount();
}
