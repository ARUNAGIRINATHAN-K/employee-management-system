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
