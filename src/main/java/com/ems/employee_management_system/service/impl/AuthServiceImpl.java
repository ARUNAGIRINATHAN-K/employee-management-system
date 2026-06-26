package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.config.JwtService;
import com.ems.employee_management_system.dto.LoginRequest;
import com.ems.employee_management_system.dto.JwtResponse;
import com.ems.employee_management_system.dto.RegisterRequest;
import com.ems.employee_management_system.dto.UserResponse;
import com.ems.employee_management_system.exception.DuplicateResourceException;
import com.ems.employee_management_system.exception.ResourceNotFoundException;
import com.ems.employee_management_system.model.Employee;
import com.ems.employee_management_system.model.Role;
import com.ems.employee_management_system.model.User;
import com.ems.employee_management_system.repository.EmployeeRepository;
import com.ems.employee_management_system.repository.RoleRepository;
import com.ems.employee_management_system.repository.UserRepository;
import com.ems.employee_management_system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for authentication and user management operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + loginRequest.getUsername()));

        String jwtToken = jwtService.generateToken(userDetails);

        Optional<Employee> linkedEmployee = employeeRepository.findByUserId(user.getId());
        Long employeeId = linkedEmployee.map(Employee::getId).orElse(null);

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return JwtResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .employeeId(employeeId)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTER (Admin only — enforced at controller layer)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + registerRequest.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException(
                    "Email '" + registerRequest.getEmail() + "' is already registered");
        }

        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName("ROLE_EMPLOYEE")
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Default role ROLE_EMPLOYEE not found. Check initialization."));
            roles.add(defaultRole);
        } else {
            for (String roleName : registerRequest.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Role not found: " + roleName));
                roles.add(role);
            }
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL USERS (Admin only)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(search, pageable);
        return users.map(this::toUserResponse);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE USER (Admin only)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteUser(Long id, String requestingUsername) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Guard 1: Admin cannot delete their own account
        if (target.getUsername().equals(requestingUsername)) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }

        // Guard 2: Cannot delete the last remaining ADMIN
        boolean targetIsAdmin = target.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (targetIsAdmin) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream()
                            .anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                    .count();
            if (adminCount <= 1) {
                throw new IllegalArgumentException(
                        "Cannot delete the last administrator account.");
            }
        }

        userRepository.delete(target);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESET PASSWORD (Admin only)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────────────────────

    private UserResponse toUserResponse(User user) {
        Optional<Employee> linkedEmp = employeeRepository.findByUserId(user.getId());
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleNames)
                .linkedEmployeeName(linkedEmp
                        .map(e -> e.getFirstName() + " " + e.getLastName())
                        .orElse(null))
                .linkedEmployeeId(linkedEmp.map(Employee::getId).orElse(null))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
