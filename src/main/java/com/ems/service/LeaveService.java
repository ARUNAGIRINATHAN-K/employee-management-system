package com.ems.service;

import com.ems.entity.Employee;
import com.ems.entity.Leave;
import com.ems.entity.LeaveBalance;
import com.ems.entity.LeavePolicy;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveBalanceRepository;
import com.ems.repository.LeaveRepository;
import com.ems.repository.LeavePolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<Leave> getAllLeaves() {
        return leaveRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Leave> getLeavesByEmployee(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Leave> getLeavesForManager(Long managerId) {
        return leaveRepository.findByManagerId(managerId);
    }

    @Transactional
    public List<LeaveBalance> getLeaveBalances(Long employeeId) {
        // Ensure default balances exist
        initializeDefaultBalancesIfNeeded(employeeId);
        return leaveBalanceRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public Leave applyLeave(Leave leave, String requesterUsername) {
        Employee employee = employeeRepository.findById(leave.getEmployee().getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        long requestedDays = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
        if (requestedDays <= 0) {
            throw new RuntimeException("Invalid leave dates. Start date must be before or equal to End date.");
        }

        initializeDefaultBalancesIfNeeded(employee.getId());

        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveType(employee.getId(), leave.getLeaveType())
                .orElseThrow(() -> new RuntimeException("Leave balance not found for type: " + leave.getLeaveType()));

        if (balance.getBalance() < requestedDays) {
            throw new RuntimeException("Insufficient leave balance. Requested: " + requestedDays + ", Available: " + balance.getBalance());
        }

        leave.setEmployee(employee);
        leave.setStatus("PENDING");
        Leave saved = leaveRepository.save(leave);

        auditLogService.log("APPLY_LEAVE", requesterUsername, 
                "Applied for " + requestedDays + " days of " + leave.getLeaveType() + " leave (ID: " + saved.getId() + ")");
        return saved;
    }

    @Transactional
    public Leave approveOrRejectLeave(Long leaveId, String status, String comments, String managerUsername) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!leave.getStatus().equals("PENDING")) {
            throw new RuntimeException("Leave request is already processed.");
        }

        Employee manager = employeeRepository.findByStatusNot("DELETED").stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(managerUsername))
                .findFirst().orElse(null);

        long requestedDays = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;

        if ("APPROVED".equalsIgnoreCase(status)) {
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveType(leave.getEmployee().getId(), leave.getLeaveType())
                    .orElseThrow(() -> new RuntimeException("Leave balance not found"));

            if (balance.getBalance() < requestedDays) {
                throw new RuntimeException("Cannot approve. Employee has insufficient leave balance.");
            }

            balance.setBalance(balance.getBalance() - (double) requestedDays);
            leaveBalanceRepository.save(balance);
            leave.setStatus("APPROVED");
        } else {
            leave.setStatus("REJECTED");
        }

        leave.setApprovedBy(manager);
        leave.setComments(comments);
        Leave updated = leaveRepository.save(leave);

        auditLogService.log("PROCESS_LEAVE", managerUsername, 
                "Leave application ID: " + leaveId + " was " + status.toUpperCase() + " by manager");
        return updated;
    }

    @Scheduled(cron = "0 0 0 1 * ?") // Midnight of 1st day of every month
    @Transactional
    public void runMonthlyAccrual() {
        System.out.println("Running automated monthly leave accrual...");
        accrueLeavesForActiveEmployees("SYSTEM");
    }

    @Transactional
    public void accrueLeavesManually(String adminUsername) {
        accrueLeavesForActiveEmployees(adminUsername);
    }

    private void accrueLeavesForActiveEmployees(String runBy) {
        List<Employee> activeEmployees = employeeRepository.findByStatusNot("DELETED");
        List<LeavePolicy> policies = leavePolicyRepository.findAll();
        for (Employee emp : activeEmployees) {
            if ("ACTIVE".equalsIgnoreCase(emp.getStatus())) {
                initializeDefaultBalancesIfNeeded(emp.getId());
                if (!policies.isEmpty()) {
                    for (LeavePolicy policy : policies) {
                        accrueType(emp, policy.getLeaveType(), policy.getMonthlyAccrualRate());
                    }
                } else {
                    accrueType(emp, "CASUAL", 1.5);
                    accrueType(emp, "SICK", 1.0);
                    accrueType(emp, "EARNED", 1.5);
                }
            }
        }
        auditLogService.log("LEAVE_ACCRUAL", runBy, "Triggered leave accrual for active employees using leave policies.");
    }

    private void accrueType(Employee employee, String leaveType, double amount) {
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveType(employee.getId(), leaveType)
                .orElseGet(() -> LeaveBalance.builder().employee(employee).leaveType(leaveType).balance(0.0).build());
        balance.setBalance(balance.getBalance() + amount);
        leaveBalanceRepository.save(balance);
    }

    private void initializeDefaultBalancesIfNeeded(Long employeeId) {
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeId(employeeId);
        if (balances.isEmpty()) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            List<LeavePolicy> policies = leavePolicyRepository.findAll();
            if (!policies.isEmpty()) {
                for (LeavePolicy policy : policies) {
                    leaveBalanceRepository.save(LeaveBalance.builder()
                            .employee(employee)
                            .leaveType(policy.getLeaveType())
                            .balance(policy.getAnnualAllocation())
                            .build());
                }
            } else {
                leaveBalanceRepository.save(LeaveBalance.builder().employee(employee).leaveType("CASUAL").balance(15.0).build());
                leaveBalanceRepository.save(LeaveBalance.builder().employee(employee).leaveType("SICK").balance(10.0).build());
                leaveBalanceRepository.save(LeaveBalance.builder().employee(employee).leaveType("EARNED").balance(15.0).build());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<LeavePolicy> getAllPolicies() {
        return leavePolicyRepository.findAll();
    }

    @Transactional
    public LeavePolicy updatePolicy(Long id, LeavePolicy policyDetails, String adminUsername) {
        LeavePolicy policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave policy not found"));
        policy.setAnnualAllocation(policyDetails.getAnnualAllocation());
        policy.setMonthlyAccrualRate(policyDetails.getMonthlyAccrualRate());
        LeavePolicy updated = leavePolicyRepository.save(policy);
        auditLogService.log("LEAVE_POLICY_UPDATE", adminUsername,
                "Updated leave policy for " + policy.getLeaveType() + 
                " (Annual: " + policy.getAnnualAllocation() + ", Monthly: " + policy.getMonthlyAccrualRate() + ")");
        return updated;
    }
}
