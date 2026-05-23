package com.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "employees")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    private Double salary;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, DELETED

    @Column(name = "photo_path")
    private String photoPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties("employees")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonIgnoreProperties("employees")
    private Employee manager;

    public Employee() {}

    public Employee(Long id, String firstName, String lastName, String email, String phone,
                    String jobTitle, LocalDate hireDate, Double salary, String status,
                    String photoPath, Department department, Employee manager) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.jobTitle = jobTitle;
        this.hireDate = hireDate;
        this.salary = salary;
        this.status = status != null ? status : "ACTIVE";
        this.photoPath = photoPath;
        this.department = department;
        this.manager = manager;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public static EmployeeBuilder builder() {
        return new EmployeeBuilder();
    }

    public static class EmployeeBuilder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String jobTitle;
        private LocalDate hireDate;
        private Double salary;
        private String status = "ACTIVE";
        private String photoPath;
        private Department department;
        private Employee manager;

        public EmployeeBuilder id(Long id) { this.id = id; return this; }
        public EmployeeBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public EmployeeBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public EmployeeBuilder email(String email) { this.email = email; return this; }
        public EmployeeBuilder phone(String phone) { this.phone = phone; return this; }
        public EmployeeBuilder jobTitle(String jobTitle) { this.jobTitle = jobTitle; return this; }
        public EmployeeBuilder hireDate(LocalDate hireDate) { this.hireDate = hireDate; return this; }
        public EmployeeBuilder salary(Double salary) { this.salary = salary; return this; }
        public EmployeeBuilder status(String status) { this.status = status; return this; }
        public EmployeeBuilder photoPath(String photoPath) { this.photoPath = photoPath; return this; }
        public EmployeeBuilder department(Department department) { this.department = department; return this; }
        public EmployeeBuilder manager(Employee manager) { this.manager = manager; return this; }

        public Employee build() {
            return new Employee(id, firstName, lastName, email, phone, jobTitle, hireDate, salary, status, photoPath, department, manager);
        }
    }
}
