import api from './api';
import type { LeaveRequest } from '../types';

export const leaveService = {
  applyLeave: (data: LeaveRequest) =>
    api.post<LeaveRequest>('/leaves', data).then((r) => r.data),

  getPersonalLeaveHistory: () =>
    api.get<LeaveRequest[]>('/leaves').then((r) => r.data),

  getPendingLeaveRequests: () =>
    api.get<LeaveRequest[]>('/leaves/pending').then((r) => r.data),

  approveLeave: (id: number) =>
    api.put<LeaveRequest>(`/leaves/${id}/approve`).then((r) => r.data),

  rejectLeave: (id: number) =>
    api.put<LeaveRequest>(`/leaves/${id}/reject`).then((r) => r.data),
};
