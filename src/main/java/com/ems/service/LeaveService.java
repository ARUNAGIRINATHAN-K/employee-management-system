package com.ems.service;

import com.ems.entity.Employee;
import com.ems.entity.Leave;
import com.ems.entity.LeaveBalance;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveBalanceRepository;
import com.ems.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional(readOnly = true)
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

            balance.setBalance(balance.getBalance() - (int) requestedDays);
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

    private void initializeDefaultBalancesIfNeeded(Long employeeId) {
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeId(employeeId);
        if (balances.isEmpty()) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            leaveBalanceRepository.save(LeaveBalance.builder().employee(employee).leaveType("CASUAL").balance(15).build());
            leaveBalanceRepository.save(LeaveBalance.builder().employee(employee).leaveType("SICK").balance(10).build());
            leaveBalanceRepository.save(LeaveBalance.builder().employee(employee).leaveType("EARNED").balance(15).build());
        }
    }
}
