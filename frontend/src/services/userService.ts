import api from './api';
import type { CreateUserRequest, UserRecord, Page } from '../types';

export interface UserParams {
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const userService = {
  /** GET /api/auth/users — paginated list of all users (admin only) */
  getAll: (params: UserParams = {}) =>
    api.get<Page<UserRecord>>('/auth/users', { params }).then((r) => r.data),

  /** POST /api/auth/register — create a new user account (admin only) */
  create: (data: CreateUserRequest) =>
    api.post<{ message: string }>('/auth/register', data).then((r) => r.data),

  /** DELETE /api/auth/users/{id} — delete user by id (admin only) */
  delete: (id: number) =>
    api.delete(`/auth/users/${id}`).then((r) => r.data),

  /** PUT /api/auth/users/{id}/password — admin reset password */
  resetPassword: (id: number, newPassword: string) =>
    api
      .put<{ message: string }>(`/auth/users/${id}/password`, { newPassword })
      .then((r) => r.data),
};
