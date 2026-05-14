package com.workforcehub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workforcehub.dto.request.LoginRequest;
import com.workforcehub.dto.response.AuthResponse;
import com.workforcehub.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.bean.override.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/login - Success")
    void login_ShouldReturnTokens() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .accessToken("test-access-token").refreshToken("test-refresh-token")
                .tokenType("Bearer").expiresIn(900000L)
                .username("admin").email("admin@test.com")
                .roles(List.of("ROLE_ADMIN")).build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder().usernameOrEmail("admin").password("Admin@123").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test-access-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Validation Error")
    void login_ShouldFailWithValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
