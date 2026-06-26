import api from './api';
import type { Department, Page } from '../types';

export const departmentService = {
  getAll: (params: { search?: string; page?: number; size?: number; sort?: string } = {}) =>
    api.get<Page<Department>>('/departments', { params }).then((r) => r.data),

  getList: () =>
    api.get<Department[]>('/departments/list').then((r) => r.data),

  getById: (id: number) =>
    api.get<Department>(`/departments/${id}`).then((r) => r.data),

  create: (data: Department) =>
    api.post<Department>('/departments', data).then((r) => r.data),

  update: (id: number, data: Department) =>
    api.put<Department>(`/departments/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    api.delete(`/departments/${id}`).then((r) => r.data),
};
