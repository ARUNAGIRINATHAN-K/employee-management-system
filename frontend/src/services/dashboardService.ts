import api from './api';
import type { DashboardStats } from '../types';

/**
 * Service to manage dashboard API operations.
 */
export const dashboardService = {
  /**
   * Fetch system-wide dashboard statistics.
   */
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get<DashboardStats>('/dashboard/stats');
    return response.data;
  },
};
