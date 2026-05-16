package com.workforcehub.repository;

import com.workforcehub.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Page<Payroll> findByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT SUM(p.netSalary) FROM Payroll p WHERE p.deleted = false AND p.paid = true")
    BigDecimal getTotalPaidSalary();

    @Query("SELECT p.employee.department.name, SUM(p.netSalary) FROM Payroll p WHERE p.deleted = false GROUP BY p.employee.department.name")
    List<Object[]> getSalaryDistributionByDepartment();

    java.util.Optional<Payroll> findFirstByEmployeeIdAndDeletedFalseOrderByPayPeriodEndDesc(Long employeeId);
}
