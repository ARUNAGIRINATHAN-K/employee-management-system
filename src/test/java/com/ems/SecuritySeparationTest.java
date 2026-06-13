package com.ems;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecuritySeparationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(username = "adminOnly", authorities = {"ROLE_ADMIN"})
    public void adminCannotRunAccrual() throws Exception {
        mvc.perform(post("/api/leaves/accrue"))
                .andExpect(status().isForbidden());
    }
}
