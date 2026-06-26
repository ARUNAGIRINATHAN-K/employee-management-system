package com.ems.employee_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data Transfer Object returned by User management endpoints.
 * Contains user credentials metadata and an optional linked employee name.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private String linkedEmployeeName;   // null if no employee record linked to this user
    private Long linkedEmployeeId;       // null if no employee record linked
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
