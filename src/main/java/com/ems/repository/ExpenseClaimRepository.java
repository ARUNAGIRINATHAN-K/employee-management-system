package com.ems.repository;

import com.ems.entity.ExpenseClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Long> {
    List<ExpenseClaim> findByEmployeeId(Long employeeId);
    List<ExpenseClaim> findByStatus(String status);

    @Query("SELECT ec FROM ExpenseClaim ec WHERE ec.employee.manager.id = :managerId")
    List<ExpenseClaim> findByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT ec FROM ExpenseClaim ec WHERE ec.employee.id = :employeeId AND ec.status = :status AND ec.claimDate BETWEEN :startDate AND :endDate")
    List<ExpenseClaim> findForPayroll(@Param("employeeId") Long employeeId,
                                      @Param("status") String status,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
}
