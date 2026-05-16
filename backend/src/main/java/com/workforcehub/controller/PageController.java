package com.workforcehub.controller;

import com.workforcehub.repository.AttendanceRepository;
import com.workforcehub.repository.PayrollRepository;
import com.workforcehub.repository.UserRepository;
import com.workforcehub.service.DashboardService;
import com.workforcehub.service.DepartmentService;
import com.workforcehub.service.EmployeeService;
import com.workforcehub.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Hidden;

@Controller
@RequiredArgsConstructor
@Hidden
public class PageController {
    private final DashboardService dashboardService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final LeaveService leaveService;
    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final com.workforcehub.repository.EmployeeRepository employeeRepository;

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() { return "forgot-password"; }

    @GetMapping("/reset-password")
    public String resetPasswordPage() { return "reset-password"; }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails user) {
        if (user != null) {
            boolean isStandardEmployee = user.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_MANAGER"));
            if (isStandardEmployee) {
                com.workforcehub.entity.User u = userRepository.findByUsername(user.getUsername()).orElse(null);
                if (u != null) {
                    com.workforcehub.entity.Employee emp = employeeRepository.findByUserId(u.getId()).orElse(null);
                    if (emp != null) {
                        model.addAttribute("employee", employeeService.getEmployeeById(emp.getId()));
                        model.addAttribute("attendanceRecords", attendanceRepository.findByEmployeeId(emp.getId(), PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "attendanceDate"))).getContent());
                        model.addAttribute("leaveRequests", leaveService.getLeavesByEmployee(emp.getId(), PageRequest.of(0, 5)).getContent());
                        model.addAttribute("todayAttendance", attendanceRepository.findTodayAttendance(emp.getId()).orElse(null));
                    }
                }
                model.addAttribute("username", user.getUsername());
                return "employee-dashboard";
            }
        }
        
        model.addAttribute("dashboard", dashboardService.getDashboardData());
        model.addAttribute("username", user != null ? user.getUsername() : "Guest");
        return "dashboard";
    }

    @GetMapping("/employees")
    public String employees(Model model, @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String search) {
        model.addAttribute("employees", employeeService.getAllEmployees(page, size, "id", "asc", search));
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        return "employees";
    }

    @GetMapping("/employees/{id}")
    public String employeeProfile(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.getEmployeeById(id));
        return "employee-profile";
    }

    @GetMapping("/employees/add")
    public String addEmployee(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "employee-form";
    }

    @GetMapping("/employees/{id}/edit")
    public String editEmployee(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.getEmployeeById(id));
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "employee-form";
    }

    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "departments";
    }

    @GetMapping("/leaves")
    public String leaves(Model model) {
        model.addAttribute("pendingLeaves", leaveService.getPendingRequests());
        return "leaves";
    }

    @GetMapping("/payroll")
    public String payroll(Model model) {
        model.addAttribute("payrolls", payrollRepository.findAll(PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "id"))).getContent());
        return "payroll";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAllActive());
        return "users";
    }

    @GetMapping("/attendance")
    public String attendance(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("attendanceRecords", attendanceRepository.findAll(PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "attendanceDate"))).getContent());
        model.addAttribute("username", user != null ? user.getUsername() : "Guest");
        return "attendance";
    }
}
