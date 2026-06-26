import api from './api';
import type { Attendance, AttendanceSummary, AttendancePolicy } from '../types';

export const attendanceService = {
  getTodayStatus: () =>
    api.get<Attendance>('/attendance/today').then((r) => r.data),

  clockIn: () =>
    api.post<Attendance>('/attendance/clock-in').then((r) => r.data),

  clockOut: () =>
    api.post<Attendance>('/attendance/clock-out').then((r) => r.data),

  getPersonalHistory: (startDate: string, endDate: string) =>
    api.get<Attendance[]>('/attendance/history', { params: { startDate, endDate } }).then((r) => r.data),

  getPersonalSummary: () =>
    api.get<AttendanceSummary>('/attendance/summary').then((r) => r.data),

  getTeamTodayStatus: () =>
    api.get<Attendance[]>('/attendance/team/today').then((r) => r.data),

  getTeamHistory: (startDate: string, endDate: string) =>
    api.get<Attendance[]>('/attendance/team/history', { params: { startDate, endDate } }).then((r) => r.data),

  getTeamSummaryCounts: () =>
    api.get<Record<string, number>>('/attendance/team/summary').then((r) => r.data),

  getPolicy: () =>
    api.get<AttendancePolicy>('/attendance-policy').then((r) => r.data),

  updatePolicy: (data: AttendancePolicy) =>
    api.put<AttendancePolicy>('/attendance-policy', data).then((r) => r.data),
};
