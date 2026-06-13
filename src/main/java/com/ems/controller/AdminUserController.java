package com.ems.controller;

import com.ems.entity.User;
import com.ems.entity.Employee;
import com.ems.repository.UserRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<User>> listAll() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");
        String role = (String) payload.get("role");
        Long employeeId = payload.get("employeeId") == null ? null : Long.valueOf(payload.get("employeeId").toString());
        if (username == null || role == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "username and role are required"));
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "username already exists"));
        }

        Employee emp = null;
        if (employeeId != null) {
            emp = employeeRepository.findById(employeeId).orElse(null);
        }
        boolean generated = false;
        if (password == null || password.isBlank()) {
            password = generateTempPassword();
            generated = true;
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .employee(emp)
                .active(true)
                .build();
        userRepository.save(user);
        auditLogService.log("CREATE_USER", username, "Created user with role: " + role);
        if (generated) {
            return ResponseEntity.ok(Map.of("user", user, "tempPassword", password));
        }
        return ResponseEntity.ok(user);
    }

    private String generateTempPassword() {
        // Simple secure random password generator (12 chars)
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%!";
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null) return ResponseEntity.badRequest().body(Map.of("message", "role required"));
        Optional<User> uopt = userRepository.findById(id);
        if (uopt.isEmpty()) return ResponseEntity.notFound().build();
        User user = uopt.get();
        String old = user.getRole();
        user.setRole(role);
        userRepository.save(user);
        auditLogService.log("ROLE_CHANGE", user.getUsername(), "Role changed from " + old + " to " + role);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        Optional<User> uopt = userRepository.findById(id);
        if (uopt.isEmpty()) return ResponseEntity.notFound().build();
        User user = uopt.get();
        user.setActive(false);
        userRepository.save(user);
        auditLogService.log("DEACTIVATE_USER", user.getUsername(), "User deactivated by admin");
        return ResponseEntity.ok(Map.of("message", "deactivated"));
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> reactivate(@PathVariable Long id) {
        Optional<User> uopt = userRepository.findById(id);
        if (uopt.isEmpty()) return ResponseEntity.notFound().build();
        User user = uopt.get();
        user.setActive(true);
        userRepository.save(user);
        auditLogService.log("REACTIVATE_USER", user.getUsername(), "User reactivated by admin");
        return ResponseEntity.ok(Map.of("message", "reactivated"));
    }
}
