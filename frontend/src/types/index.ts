// ─── Auth Types ───────────────────────────────────────────────────────────────
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  roles?: string[];
}

export interface JwtResponse {
  token: string;
  username: string;
  email: string;
  roles: string[];
  employeeId: number | null;
}

export interface AuthUser {
  username: string;
  email: string;
  roles: string[];
  employeeId: number | null;
}

// ─── Department Types ─────────────────────────────────────────────────────────
export interface Department {
  id?: number;
  name: string;
  description?: string;
  employeeCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

// ─── Employee Types ───────────────────────────────────────────────────────────
export type EmployeeStatus = 'ACTIVE' | 'INACTIVE';

export interface Employee {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  jobTitle: string;
  salary: number;
  hireDate: string;   // ISO date string yyyy-MM-dd
  status: EmployeeStatus;
  departmentId: number;
  departmentName?: string;
  userId?: number;
  username?: string;
  createdAt?: string;
  updatedAt?: string;
}

// ─── Pagination Types ─────────────────────────────────────────────────────────
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;       // 0-indexed page number
  size: number;
  first: boolean;
  last: boolean;
}

// ─── Error Response ───────────────────────────────────────────────────────────
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}

// ─── Dashboard Types ──────────────────────────────────────────────────────────
export interface DashboardStats {
  totalEmployees: number;
  activeEmployees: number;
  totalDepartments: number;
  averageSalary: number;
  employeesPerDepartment: Record<string, number>;
  departmentName?: string;
  departmentId?: number;
  teamDistribution?: Record<string, number>;
}


// ─── User Management Types ────────────────────────────────────────────────────
export interface UserRecord {
  id: number;
  username: string;
  email: string;
  roles: string[];
  linkedEmployeeName: string | null;
  linkedEmployeeId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  roles: string[];
}

// ─── Attendance & Leave Types ────────────────────────────────────────────────
export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'ON_LEAVE' | 'WFH' | 'WEEKEND' | 'HOLIDAY';
export type WorkMode = 'OFFICE' | 'REMOTE';
export type LeaveType = 'CASUAL' | 'SICK' | 'WFH' | 'PERMISSION';
export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface AttendancePolicy {
  id?: number;
  shiftStartTime: string; // "HH:mm:ss"
  shiftEndTime: string;   // "HH:mm:ss"
  gracePeriodMinutes: number;
  overtimeThresholdMinutes: number;
}

export interface Attendance {
  id?: number;
  employeeId?: number;
  employeeName?: string;
  date: string;         // ISO date string yyyy-MM-dd
  clockIn?: string;      // ISO datetime string
  clockOut?: string;     // ISO datetime string
  status: AttendanceStatus;
  workMode: WorkMode;
  overtimeMinutes: number;
  lateMinutes: number;
}

export interface AttendanceSummary {
  presentDays: number;
  lateDays: number;
  absentDays: number;
  leaveDays: number;
  wfhDays: number;
  weekendDays: number;
  holidayDays: number;
  totalOvertimeMinutes: number;
  totalLateMinutes: number;
}


export interface LeaveRequest {
  id?: number;
  employeeId?: number;
  employeeName?: string;
  leaveType: LeaveType;
  startDate: string;    // ISO date string yyyy-MM-dd
  endDate: string;      // ISO date string yyyy-MM-dd
  reason: string;
  status: LeaveStatus;
  approvedByUsername?: string;
  createdAt?: string;
}
