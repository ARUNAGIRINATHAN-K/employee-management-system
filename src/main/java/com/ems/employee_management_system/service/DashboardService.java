package com.ems.employee_management_system.service;

import com.ems.employee_management_system.dto.DashboardStatsDTO;

/**
 * Service interface for retrieving dashboard statistics.
 */
public interface DashboardService {
    /**
     * Retrieve the aggregated dashboard statistics.
     * @return DashboardStatsDTO containing counts and aggregates.
     */
    DashboardStatsDTO getDashboardStats();
}
