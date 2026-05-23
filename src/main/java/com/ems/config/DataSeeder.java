package com.ems.config;

import com.ems.entity.Department;
import com.ems.entity.Employee;
import com.ems.entity.User;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only seed if the database is empty
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println("========================================");
        System.out.println("  Seeding initial data...");
        System.out.println("========================================");

        // Create departments
        Department hrDept = departmentRepository.save(
                Department.builder().name("Human Resources").description("HR and People Management").build());
        Department engDept = departmentRepository.save(
                Department.builder().name("Engineering").description("Software Development & IT").build());
        Department salesDept = departmentRepository.save(
                Department.builder().name("Sales").description("Sales and Business Development").build());
        Department financeDept = departmentRepository.save(
                Department.builder().name("Finance").description("Accounting and Financial Planning").build());
        Department marketingDept = departmentRepository.save(
                Department.builder().name("Marketing").description("Marketing and Communications").build());

        // Create HR Admin employee
        Employee hrAdmin = employeeRepository.save(Employee.builder()
                .firstName("Admin").lastName("HR")
                .email("admin").phone("9876543210")
                .jobTitle("HR Director").salary(95000.0)
                .hireDate(LocalDate.of(2020, 1, 15))
                .status("ACTIVE").department(hrDept).build());

        // Create Manager employee
        Employee manager = employeeRepository.save(Employee.builder()
                .firstName("Priya").lastName("Sharma")
                .email("manager").phone("9876543211")
                .jobTitle("Engineering Manager").salary(85000.0)
                .hireDate(LocalDate.of(2021, 3, 10))
                .status("ACTIVE").department(engDept).build());

        // Create regular employee
        Employee emp1 = employeeRepository.save(Employee.builder()
                .firstName("Rahul").lastName("Kumar")
                .email("employee").phone("9876543212")
                .jobTitle("Software Engineer").salary(65000.0)
                .hireDate(LocalDate.of(2022, 6, 1))
                .status("ACTIVE").department(engDept).manager(manager).build());

        // Additional sample employees
        Employee emp2 = employeeRepository.save(Employee.builder()
                .firstName("Anita").lastName("Desai")
                .email("anita.desai@ems.com").phone("9876543213")
                .jobTitle("Senior Developer").salary(75000.0)
                .hireDate(LocalDate.of(2021, 8, 20))
                .status("ACTIVE").department(engDept).manager(manager).build());

        Employee emp3 = employeeRepository.save(Employee.builder()
                .firstName("Vikram").lastName("Singh")
                .email("vikram.singh@ems.com").phone("9876543214")
                .jobTitle("Sales Executive").salary(55000.0)
                .hireDate(LocalDate.of(2023, 1, 5))
                .status("ACTIVE").department(salesDept).build());

        Employee emp4 = employeeRepository.save(Employee.builder()
                .firstName("Neha").lastName("Patel")
                .email("neha.patel@ems.com").phone("9876543215")
                .jobTitle("Financial Analyst").salary(60000.0)
                .hireDate(LocalDate.of(2022, 11, 15))
                .status("ACTIVE").department(financeDept).build());

        Employee emp5 = employeeRepository.save(Employee.builder()
                .firstName("Arjun").lastName("Reddy")
                .email("arjun.reddy@ems.com").phone("9876543216")
                .jobTitle("Marketing Specialist").salary(58000.0)
                .hireDate(LocalDate.of(2023, 4, 1))
                .status("ACTIVE").department(marketingDept).build());

        Employee emp6 = employeeRepository.save(Employee.builder()
                .firstName("Deepa").lastName("Nair")
                .email("deepa.nair@ems.com").phone("9876543217")
                .jobTitle("HR Executive").salary(50000.0)
                .hireDate(LocalDate.of(2023, 7, 10))
                .status("ACTIVE").department(hrDept).manager(hrAdmin).build());

        // Set department managers
        hrDept.setManager(hrAdmin);
        departmentRepository.save(hrDept);
        engDept.setManager(manager);
        departmentRepository.save(engDept);

        // Create user accounts
        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role("ROLE_HR")
                .employee(hrAdmin).build());

        userRepository.save(User.builder()
                .username("manager")
                .password(passwordEncoder.encode("manager123"))
                .role("ROLE_MANAGER")
                .employee(manager).build());

        userRepository.save(User.builder()
                .username("employee")
                .password(passwordEncoder.encode("employee123"))
                .role("ROLE_EMPLOYEE")
                .employee(emp1).build());

        System.out.println("========================================");
        System.out.println("  Data seeding complete!");
        System.out.println("  Login credentials:");
        System.out.println("  HR Admin:  admin / admin123");
        System.out.println("  Manager:   manager / manager123");
        System.out.println("  Employee:  employee / employee123");
        System.out.println("========================================");
    }
}
