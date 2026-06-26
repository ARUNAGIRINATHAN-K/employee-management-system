package com.ems.employee_management_system.controller;

import com.ems.employee_management_system.dto.LoginRequest;
import com.ems.employee_management_system.dto.JwtResponse;
import com.ems.employee_management_system.dto.PasswordResetRequest;
import com.ems.employee_management_system.dto.RegisterRequest;
import com.ems.employee_management_system.dto.UserResponse;
import com.ems.employee_management_system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller handling Authentication and User Management requests.
 *
 * <p><b>RBAC Permission Matrix:</b></p>
 * <ul>
 *   <li>POST /login       — public</li>
 *   <li>POST /register    — ADMIN only</li>
 *   <li>GET  /users       — ADMIN only</li>
 *   <li>DELETE /users/{id}       — ADMIN only</li>
 *   <li>PUT /users/{id}/password — ADMIN only</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService authService;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC — Login
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/auth/login
     * Authenticate user credentials and return a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN — Create User Account
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/auth/register
     * Register a new user credential account. Restricted to ADMIN role only.
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return new ResponseEntity<>(
                Map.of("message", "User registered successfully"),
                HttpStatus.CREATED);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN — List Users
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/auth/users
     * Returns a paginated, searchable list of all user accounts.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(value = "search", required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(authService.getAllUsers(search, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN — Delete User
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * DELETE /api/auth/users/{id}
     * Delete a user account by ID. Cannot delete own account or last admin.
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        authService.deleteUser(id, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN — Reset Password
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * PUT /api/auth/users/{id}/password
     * Admin resets a user's password.
     */
    @PutMapping("/users/{id}/password")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}
