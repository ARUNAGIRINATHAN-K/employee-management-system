import axios, { AxiosError } from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';
import { tokenUtils } from '../utils/tokenUtils';

// ─── Base Axios Instance ──────────────────────────────────────────────────────
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// ─── Request Interceptor – attach JWT ─────────────────────────────────────────
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = tokenUtils.getToken();
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// ─── Response Interceptor – handle 401 / token refresh stub ──────────────────
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      // ── Token Refresh Preparation ──────────────────────────────────────────
      // When a refresh-token endpoint is added to the backend, replace the
      // block below with the actual refresh call:
      //
      //   try {
      //     const refreshToken = tokenUtils.getRefreshToken();
      //     const { data } = await axios.post('/api/auth/refresh', { refreshToken });
      //     tokenUtils.setToken(data.token);
      //     originalRequest.headers['Authorization'] = `Bearer ${data.token}`;
      //     return api(originalRequest);
      //   } catch { /* fall through to logout */ }
      //
      // ── For now: clear session and redirect to /login ──────────────────────
      tokenUtils.clear();
      window.location.href = '/login';
    }

    return Promise.reject(error);
  }
);

export default api;
