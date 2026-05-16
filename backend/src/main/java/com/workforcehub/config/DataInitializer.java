package com.workforcehub.config;

import com.workforcehub.entity.Department;
import com.workforcehub.entity.Employee;
import com.workforcehub.entity.Role;
import com.workforcehub.entity.User;
import com.workforcehub.enums.EmployeeStatus;
import com.workforcehub.enums.Gender;
import com.workforcehub.enums.RoleType;
import com.workforcehub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Data initializer that seeds the database with default roles, admin user, and sample data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initDepartments();
        initAdminUser();
        initEmployeeUser();
        log.info("✅ Data initialization completed successfully");
    }

    private void initRoles() {
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByName(roleType)) {
                Role role = new Role();
                role.setName(roleType);
                role.setDescription(roleType.name().replace("ROLE_", "") + " role");
                roleRepository.save(role);
                log.info("Created role: {}", roleType);
            }
        }
    }

    private void initDepartments() {
        String[][] departments = {
                {"Engineering", "ENG", "Software Engineering Department"},
                {"Human Resources", "HR", "Human Resources Department"},
                {"Finance", "FIN", "Finance and Accounting Department"},
                {"Marketing", "MKT", "Marketing and Communications Department"},
                {"Operations", "OPS", "Operations and Logistics Department"},
                {"Sales", "SLS", "Sales Department"},
                {"IT Support", "ITS", "IT Infrastructure and Support"},
                {"Legal", "LGL", "Legal and Compliance Department"}
        };

        for (String[] dept : departments) {
            if (!departmentRepository.existsByCode(dept[1])) {
                Department department = Department.builder()
                        .name(dept[0])
                        .code(dept[1])
                        .description(dept[2])
                        .active(true)
                        .build();
                departmentRepository.save(department);
                log.info("Created department: {}", dept[0]);
            }
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@workforcehub.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .enabled(true)
                    .emailVerified(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(adminUser);

            Department engDept = departmentRepository.findByCode("ENG").orElse(null);

            Employee adminEmployee = Employee.builder()
                    .employeeId("EMP001")
                    .firstName("System")
                    .lastName("Administrator")
                    .email("admin@workforcehub.com")
                    .phone("+1234567890")
                    .gender(Gender.PREFER_NOT_TO_SAY)
                    .designation("System Administrator")
                    .joiningDate(LocalDate.now())
                    .status(EmployeeStatus.ACTIVE)
                    .salary(new BigDecimal("120000.00"))
                    .department(engDept)
                    .user(adminUser)
                    .build();
            employeeRepository.save(adminEmployee);

            log.info("✅ Admin user created - username: admin, password: Admin@123");
        }
    }

    private void initEmployeeUser() {
        if (!userRepository.existsByUsername("employee")) {
            Role empRole = roleRepository.findByName(RoleType.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new RuntimeException("Employee role not found"));

            User empUser = User.builder()
                    .username("employee")
                    .email("employee@workforcehub.com")
                    .password(passwordEncoder.encode("Employee@123"))
                    .enabled(true)
                    .emailVerified(true)
                    .roles(Set.of(empRole))
                    .build();
            userRepository.save(empUser);

            Department mktDept = departmentRepository.findByCode("MKT").orElse(null);

            Employee standardEmployee = Employee.builder()
                    .employeeId("EMP002")
                    .firstName("Jane")
                    .lastName("Doe")
                    .email("employee@workforcehub.com")
                    .phone("+1987654321")
                    .gender(Gender.FEMALE)
                    .designation("Marketing Specialist")
                    .joiningDate(LocalDate.now().minusMonths(6))
                    .status(EmployeeStatus.ACTIVE)
                    .salary(new BigDecimal("60000.00"))
                    .department(mktDept)
                    .user(empUser)
                    .build();
            employeeRepository.save(standardEmployee);

            log.info("✅ Employee user created - username: employee, password: Employee@123");
        }
    }
}
