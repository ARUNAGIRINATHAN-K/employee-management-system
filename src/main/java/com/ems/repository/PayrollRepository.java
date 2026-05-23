package com.ems.repository;

import com.ems.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByEmployeeId(Long employeeId);
    Optional<Payroll> findByEmployeeIdAndPayPeriod(Long employeeId, String payPeriod);
    List<Payroll> findByPayPeriod(String payPeriod);
}
