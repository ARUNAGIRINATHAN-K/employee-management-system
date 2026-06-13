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
import org.springframework.security.access.AccessDeniedException;

import com.ems.repository.UserRepository;
import com.ems.entity.User;
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
    private UserRepository userRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

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
        List<Leave> leaves = leaveRepository.findByManagerId(managerId);
        // Also include leaves for employees in the manager's department
        Employee manager = employeeRepository.findById(managerId).orElse(null);
        if (manager != null && manager.getDepartment() != null) {
            Long deptId = manager.getDepartment().getId();
            List<Leave> deptLeaves = leaveRepository.findByEmployeeDepartmentId(deptId);
            // merge without duplicates
            for (Leave l : deptLeaves) {
                if (!leaves.contains(l)) leaves.add(l);
            }
        }
        return leaves;
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
        notificationService.sendNotification("New leave application submitted by " + employee.getFirstName() + " " + employee.getLastName());
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

        // Security: only HR or managers of the same department may approve/reject
        User acting = userRepository.findByUsername(managerUsername).orElse(null);
        boolean isHr = acting != null && "ROLE_HR".equals(acting.getRole());
        if (!isHr) {
            // if not HR, must be manager and belong to same department
            if (manager == null) {
                throw new AccessDeniedException("You are not authorized to process this leave request");
            }
            Long managerDeptId = manager.getDepartment() != null ? manager.getDepartment().getId() : null;
            Long employeeDeptId = leave.getEmployee() != null && leave.getEmployee().getDepartment() != null
                    ? leave.getEmployee().getDepartment().getId() : null;
            if (managerDeptId == null || employeeDeptId == null || !managerDeptId.equals(employeeDeptId)) {
                throw new AccessDeniedException("Managers may only process leave requests for employees in their own department");
            }
        }

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
        notificationService.sendNotification("Leave request #" + leaveId + " has been " + status.toUpperCase() + " for " + leave.getEmployee().getFirstName() + " " + leave.getEmployee().getLastName() + ".");
        return updated;
    }

    @Scheduled(cron = "0 0 0 1 * ?") // Midnight of 1st day of every month
    @Transactional
    public void runMonthlyAccrual() {
        System.out.println("Running automated monthly leave accrual...");
        accrueLeavesForActiveEmployees("SYSTEM");
    }

    @Transactional
    public void accrueLeavesManually(String initiatedBy) {
        accrueLeavesForActiveEmployees(initiatedBy);
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
        notificationService.sendNotification("Manual leave accrual has been processed for active employees.");
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
        public LeavePolicy updatePolicy(Long id, LeavePolicy policyDetails, String initiatedBy) {
        LeavePolicy policy = leavePolicyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave policy not found"));
        policy.setAnnualAllocation(policyDetails.getAnnualAllocation());
        policy.setMonthlyAccrualRate(policyDetails.getMonthlyAccrualRate());
        LeavePolicy updated = leavePolicyRepository.save(policy);
        auditLogService.log("LEAVE_POLICY_UPDATE", initiatedBy,
            "Updated leave policy for " + policy.getLeaveType() + 
            " (Annual: " + policy.getAnnualAllocation() + ", Monthly: " + policy.getMonthlyAccrualRate() + ")");
        notificationService.sendNotification("Leave policy for " + policy.getLeaveType() + " has been updated.");
        return updated;
    }
}
