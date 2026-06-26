package com.ems.employee_management_system.service.impl;

import com.ems.employee_management_system.config.JwtService;
import com.ems.employee_management_system.dto.LoginRequest;
import com.ems.employee_management_system.dto.JwtResponse;
import com.ems.employee_management_system.dto.RegisterRequest;
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
 * Service implementation for authentication and registration operations.
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

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        // Authenticate credentials through Spring Security AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + loginRequest.getUsername()));

        // Generate JWT token
        String jwtToken = jwtService.generateToken(userDetails);

        // Fetch optional linked employee profile ID
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

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        // Validation: Verify uniqueness of username and email
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username '" + registerRequest.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email '" + registerRequest.getEmail() + "' is already registered");
        }

        // Map roles. Default to ROLE_EMPLOYEE if none provided
        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName("ROLE_EMPLOYEE")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_EMPLOYEE not found in database. Check initialization."));
            roles.add(defaultRole);
        } else {
            for (String roleName : registerRequest.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found in database: " + roleName));
                roles.add(role);
            }
        }

        // Create new User entity with encrypted password
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);
    }
}
