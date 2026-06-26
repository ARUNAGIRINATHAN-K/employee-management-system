package com.ems.employee_management_system.config;

import com.ems.employee_management_system.model.Role;
import com.ems.employee_management_system.model.User;
import com.ems.employee_management_system.repository.RoleRepository;
import com.ems.employee_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

/**
 * Foolproof programmatic database seeder to initialize default security roles and default admin credentials.
 */
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking database role and administrator seeding...");

        // 1. Seed Roles
        seedRole("ROLE_ADMIN", "Full system access: manage employees, departments, users, and system settings");
        seedRole("ROLE_HR", "HR access: full employee CRUD and department view only");
        seedRole("ROLE_MANAGER", "Manager access: view employees in own department, read-only departments");
        seedRole("ROLE_EMPLOYEE", "Employee access: view and update own profile only");

        // 2. Seed Default Admin User
        if (!userRepository.existsByUsername("admin")) {
            log.info("Seeding default admin user (admin / admin123)...");
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found after seeding roles!"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .username("admin")
                    .email("admin@ems.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            log.info("Default admin user successfully seeded.");
        } else {
            log.info("Admin user already exists. Skipping seeding.");
        }
    }

    private void seedRole(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            log.info("Seeding role: {}", name);
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();
            roleRepository.save(role);
        }
    }
}
