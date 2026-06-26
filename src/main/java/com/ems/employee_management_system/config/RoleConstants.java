package com.ems.employee_management_system.config;

/**
 * Constants for all authorization role names used in {@code @PreAuthorize} expressions.
 * Centralising them here prevents typos and keeps rules easy to audit.
 */
public final class RoleConstants {

    private RoleConstants() {}

    public static final String ADMIN    = "ROLE_ADMIN";
    public static final String HR       = "ROLE_HR";
    public static final String MANAGER  = "ROLE_MANAGER";
    public static final String EMPLOYEE = "ROLE_EMPLOYEE";
}
