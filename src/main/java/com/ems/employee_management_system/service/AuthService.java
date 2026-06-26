package com.ems.employee_management_system.service;

import com.ems.employee_management_system.dto.LoginRequest;
import com.ems.employee_management_system.dto.JwtResponse;
import com.ems.employee_management_system.dto.RegisterRequest;

/**
 * Service interface for authentication and registration operations.
 */
public interface AuthService {
    
    /**
     * Authenticates credentials and returns a signed JWT token with user metadata.
     */
    JwtResponse login(LoginRequest loginRequest);
    
    /**
     * Registers a new user account with encrypted passwords and roles assignment.
     */
    void register(RegisterRequest registerRequest);
}
