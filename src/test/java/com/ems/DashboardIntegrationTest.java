package com.ems;

import com.ems.dto.LoginRequest;
import com.ems.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DashboardIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private LoginResponse login(String username, String password) {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        ResponseEntity<LoginResponse> res = restTemplate.postForEntity("/api/auth/login", req, LoginResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        return res.getBody();
    }

    @Test
    public void managerShouldSeeScopedDashboard() {
        LoginResponse manager = login("manager", "manager123");
        assertThat(manager).isNotNull();
        String token = manager.getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> statsRes = restTemplate.exchange("/api/dashboard/stats", HttpMethod.GET, entity, new ParameterizedTypeReference<>(){});
        assertThat(statsRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> stats = statsRes.getBody();
        assertThat(stats).containsKeys("totalEmployees", "activeEmployees", "departmentWiseCounts", "totalDepartments");
        // Manager should see single department
        Object totalDepts = stats.get("totalDepartments");
        assertThat(((Number) totalDepts).intValue()).isEqualTo(1);
    }

    @Test
    public void hrShouldSeeGlobalDashboard() {
        LoginResponse hr = login("admin", "admin123");
        assertThat(hr).isNotNull();
        String token = hr.getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> statsRes = restTemplate.exchange("/api/dashboard/stats", HttpMethod.GET, entity, new ParameterizedTypeReference<>(){});
        assertThat(statsRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> stats = statsRes.getBody();
        assertThat(stats).containsKeys("totalEmployees", "activeEmployees", "departmentWiseCounts", "totalDepartments");
        Object totalDepts = stats.get("totalDepartments");
        assertThat(((Number) totalDepts).intValue()).isGreaterThanOrEqualTo(1);
    }
}
