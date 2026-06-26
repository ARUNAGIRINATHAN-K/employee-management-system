import api from './api';
import type { Employee, Page } from '../types';

export interface EmployeeParams {
  search?: string;
  departmentId?: number;
  status?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const employeeService = {
  getAll: (params: EmployeeParams = {}) =>
    api.get<Page<Employee>>('/employees', { params }).then((r) => r.data),

  getById: (id: number) =>
    api.get<Employee>(`/employees/${id}`).then((r) => r.data),

  getByUserId: (userId: number) =>
    api.get<Employee>(`/employees/user/${userId}`).then((r) => r.data),

  create: (data: Employee) =>
    api.post<Employee>('/employees', data).then((r) => r.data),

  update: (id: number, data: Employee) =>
    api.put<Employee>(`/employees/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    api.delete(`/employees/${id}`).then((r) => r.data),

  /** POST /employees/{id}/account — create + link a login account to this employee */
  assignAccount: (id: number, data: { username: string; password: string; role: string }) =>
    api.post<Employee>(`/employees/${id}/account`, data).then((r) => r.data),
};

