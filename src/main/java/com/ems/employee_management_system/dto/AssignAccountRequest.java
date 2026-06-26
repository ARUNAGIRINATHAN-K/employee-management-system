package com.ems.employee_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for assigning a new login account to an existing employee.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignAccountRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    /** Role to assign. E.g. ROLE_EMPLOYEE, ROLE_MANAGER, ROLE_HR, ROLE_ADMIN */
    @NotBlank(message = "Role is required")
    private String role;
}
