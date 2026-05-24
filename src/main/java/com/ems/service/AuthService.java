package com.ems.service;

import com.ems.dto.LoginRequest;
import com.ems.dto.LoginResponse;
import com.ems.dto.ResetPasswordRequest;
import com.ems.entity.User;
import com.ems.repository.UserRepository;
import com.ems.security.JwtUtils;
import com.ems.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuditLogService auditLogService;

    // Stores email -> resetToken mapping
    private final Map<String, String> resetTokens = new ConcurrentHashMap<>();

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        auditLogService.log("LOGIN", userPrincipal.getUsername(), "Successfully logged in");

        return new LoginResponse(
                jwt,
                userPrincipal.getUsername(),
                userPrincipal.getRole(),
                userPrincipal.getId(),
            userPrincipal.getUser().getEmployee() != null ? userPrincipal.getUser().getEmployee().getId() : null,
            userPrincipal.getDepartmentId()
        );
    }

    public String generateResetToken(String email) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("No user associated with this email"));
        
        String token = UUID.randomUUID().toString().substring(0, 6).toUpperCase(); // 6 character code
        resetTokens.put(email, token);
        
        auditLogService.log("FORGOT_PASSWORD_REQUEST", email, "Generated reset token: " + token);
        System.out.println("Password Reset Token for " + email + " is: " + token);
        return token;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String savedToken = resetTokens.get(request.getEmail());
        if (savedToken == null || !savedToken.equals(request.getToken())) {
            throw new RuntimeException("Invalid or expired password reset token");
        }

        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetTokens.remove(request.getEmail());
        auditLogService.log("RESET_PASSWORD", request.getEmail(), "Password reset successfully");
    }
}
