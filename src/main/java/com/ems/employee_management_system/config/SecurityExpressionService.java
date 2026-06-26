package com.ems.employee_management_system.config;

import com.ems.employee_management_system.model.Employee;
import com.ems.employee_management_system.repository.EmployeeRepository;
import com.ems.employee_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;


/**
 * Custom bean providing reusable Spring Security SpEL expressions for fine-grained
 * access control in {@code @PreAuthorize} annotations.
 *
 * <p>Referenced via the {@code @sec} bean alias in SpEL, e.g.:
 * {@code @PreAuthorize("@sec.isOwnerOrAdmin(#id)")}
 * </p>
 */
@Component("sec")
@RequiredArgsConstructor
public class SecurityExpressionService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Returns {@code true} when the currently authenticated user is either
     * ROLE_ADMIN / ROLE_HR / ROLE_MANAGER, OR is the Employee whose record id
     * matches the supplied {@code employeeId}.
     *
     * <p>Used to protect GET /api/employees/{id} – HR and above can see all;
     * plain ROLE_EMPLOYEE can only see their own record.</p>
     */
    public boolean isOwnerOrPrivileged(Long employeeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        boolean isAdminOrHR = auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String r = a.getAuthority();
                    return r.equals(RoleConstants.ADMIN) ||
                           r.equals(RoleConstants.HR);
                });

        if (isAdminOrHR) return true;

        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleConstants.MANAGER));

        String username = auth.getName();
        Optional<com.ems.employee_management_system.model.User> currentUserOpt = userRepository.findByUsername(username);
        if (!currentUserOpt.isPresent()) return false;

        Long currentUserId = currentUserOpt.get().getId();
        Optional<Employee> currentEmpOpt = employeeRepository.findByUserId(currentUserId);

        if (isManager && currentEmpOpt.isPresent()) {
            Employee managerEmp = currentEmpOpt.get();
            if (managerEmp.getDepartment() != null) {
                Long managerDeptId = managerEmp.getDepartment().getId();
                // Resolve target employee
                Optional<Employee> targetEmpOpt = employeeRepository.findById(employeeId);
                if (targetEmpOpt.isPresent()) {
                    Employee targetEmp = targetEmpOpt.get();
                    if (targetEmp.getDepartment() != null && targetEmp.getDepartment().getId().equals(managerDeptId)) {
                        return true; // Manager is in the same department
                    }
                }
            }
        }

        // Fallback: check if this employee record belongs to the authenticated user (for ROLE_EMPLOYEE or self-access)
        return currentEmpOpt
                .map(Employee::getId)
                .map(ownId -> ownId.equals(employeeId))
                .orElse(false);
    }


    /**
     * Returns {@code true} when the currently authenticated user is accessing
     * their own user-linked employee profile.
     *
     * <p>Used to protect GET /api/employees/user/{userId}.</p>
     */
    public boolean isSelfOrPrivileged(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        boolean isPrivileged = auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String r = a.getAuthority();
                    return r.equals(RoleConstants.ADMIN) ||
                           r.equals(RoleConstants.HR)    ||
                           r.equals(RoleConstants.MANAGER);
                });

        if (isPrivileged) return true;

        String username = auth.getName();
        return userRepository.findByUsername(username)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }
}
