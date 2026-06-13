package com.ems.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // e.g., ROLE_HR, ROLE_MANAGER, ROLE_EMPLOYEE

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private Boolean active = true;

        @ManyToMany
        @JoinTable(name = "user_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
        private java.util.Set<com.ems.entity.SecurityGroup> groups = new java.util.HashSet<>();

    public User() {}

    public User(Long id, String username, String password, String role, Employee employee) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public java.util.Set<com.ems.entity.SecurityGroup> getGroups() { return groups; }
    public void setGroups(java.util.Set<com.ems.entity.SecurityGroup> groups) { this.groups = groups; }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String password;
        private String role;
        private Employee employee;
        private Boolean active;
        private java.util.Set<com.ems.entity.SecurityGroup> groups;

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder username(String username) { this.username = username; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder role(String role) { this.role = role; return this; }
        public UserBuilder employee(Employee employee) { this.employee = employee; return this; }
        public UserBuilder active(Boolean active) { this.active = active; return this; }
        public UserBuilder groups(java.util.Set<com.ems.entity.SecurityGroup> groups) { this.groups = groups; return this; }

        public User build() {
            User u = new User(id, username, password, role, employee);
            u.setActive(this.active != null ? this.active : true);
            if (this.groups != null) u.setGroups(this.groups);
            return u;
        }
    }
}
