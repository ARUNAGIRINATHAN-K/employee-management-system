import api from './api';
import type { LoginRequest, JwtResponse, RegisterRequest } from '../types';

export const authService = {
  login: (data: LoginRequest) =>
    api.post<JwtResponse>('/auth/login', data).then((r) => r.data),

  register: (data: RegisterRequest) =>
    api.post<{ message: string }>('/auth/register', data).then((r) => r.data),
};
