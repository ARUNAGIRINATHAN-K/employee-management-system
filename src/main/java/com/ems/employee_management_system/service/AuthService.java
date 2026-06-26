package com.ems.employee_management_system.service;

import com.ems.employee_management_system.dto.LoginRequest;
import com.ems.employee_management_system.dto.JwtResponse;
import com.ems.employee_management_system.dto.RegisterRequest;
import com.ems.employee_management_system.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for authentication and user management operations.
 */
public interface AuthService {

    /**
     * Authenticates credentials and returns a signed JWT token with user metadata.
     */
    JwtResponse login(LoginRequest loginRequest);

    /**
     * Registers a new user account with encrypted passwords and roles assignment.
     * Restricted to ADMIN callers.
     */
    void register(RegisterRequest registerRequest);

    /**
     * Returns a paginated list of all user accounts with optional search filter.
     * Restricted to ADMIN callers.
     */
    Page<UserResponse> getAllUsers(String search, Pageable pageable);

    /**
     * Deletes a user account by ID.
     * Guards: cannot delete own account; cannot delete the last ADMIN.
     */
    void deleteUser(Long id, String requestingUsername);

    /**
     * Resets the password of any user account.
     * Restricted to ADMIN callers.
     */
    void resetPassword(Long id, String newPassword);
}
