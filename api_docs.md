# API Documentation and Frontend Integration Map

## Purpose
This document maps the static UI pages and frontend components to backend REST API endpoints. It also flags areas of current backend integration and frontend gaps for future implementation.

## Frontend Pages and Primary API Coverage

### `src/main/resources/static/index.html`
- Authentication page with login, forgot password, and reset password flows.
- Uses:
  - `POST /api/auth/login`
  - `POST /api/auth/forgot-password`
  - `POST /api/auth/reset-password`
- Backend integration status: **Implemented**

### `src/main/resources/static/dashboard.html`
- Main SPA dashboard and management console.
- Uses `src/main/resources/static/js/app.js` for navigation and section loading.
- Primary backend endpoints used:
  - `GET /api/dashboard/stats`
  - `GET /api/employees?page=&size=&sortBy=&sortDir=&search=`
  - `GET /api/employees/{id}`
  - `POST /api/employees`
  - `PUT /api/employees/{id}`
  - `DELETE /api/employees/{id}`
  - `POST /api/employees/{id}/photo`
  - `GET /api/employees/export/excel`
  - `GET /api/employees/export/pdf`
  - `GET /api/departments`
  - `GET /api/departments/{id}`
  - `GET /api/departments/{id}/employees`
  - `POST /api/departments`
  - `PUT /api/departments/{id}`
  - `DELETE /api/departments/{id}`
  - `GET /api/shifts`
  - `GET /api/leaves`
  - `GET /api/leaves/employee/{employeeId}`
  - `GET /api/leaves/manager/{managerId}`
  - `GET /api/leaves/balances/employee/{employeeId}`
  - `POST /api/leaves/apply`
  - `POST /api/leaves/{id}/approve?status=&comments=`
  - `POST /api/leaves/accrue`
  - `GET /api/leaves/policies`
  - `PUT /api/leaves/policies/{id}`
  - `GET /api/profile-changes/my-requests`
  - `GET /api/profile-changes/pending`
  - `POST /api/profile-changes`
  - `POST /api/profile-changes/{id}/approve`
  - `POST /api/profile-changes/{id}/reject`
  - `GET /api/expenses`
  - `GET /api/expenses/employee/{employeeId}`
  - `GET /api/expenses/manager/{managerId}`
  - `POST /api/expenses`
  - `POST /api/expenses/{id}/approve`
  - `POST /api/expenses/{id}/reject`
  - `POST /api/attendance/check-in/{employeeId}`
  - `POST /api/attendance/check-out/{employeeId}`
  - `GET /api/attendance/today/{employeeId}`
  - `GET /api/attendance/history/{employeeId}`
  - `GET /api/payroll/history/{employeeId}`
  - `GET /api/payroll/period?period=`
  - `POST /api/payroll/generate?employeeId=&payPeriod=`
  - `POST /api/payroll/{id}/pay`
  - `GET /api/payroll/{id}/payslip`
  - `GET /api/performance/employee/{employeeId}`
  - `POST /api/performance`
  - `GET /api/notifications/subscribe?token=`
- Backend integration status: **Mostly implemented**
  - Dashboard, employees, departments, leave, attendance, payroll, performance, profile, and expense sections are wired.
  - Notification SSE subscription is present in `app.js`.

### `src/main/resources/static/admin-dashboard.html`
- Admin console page for user management, reporting, and system configuration.
- Uses `src/main/resources/static/js/admin.js`.
- Primary backend endpoints used:
  - `GET /api/admin/users`
  - `POST /api/admin/users/{id}/deactivate`
  - `POST /api/admin/users/{id}/reactivate`
  - `PUT /api/admin/users/{id}/role`
  - `GET /api/admin/export/audit-logs`
  - `GET /api/admin/export/company-directory`
- Backend integration status: **Partially implemented**
  - User management and exports are implemented.
  - UI placeholders exist for holidays and system configuration, but the current JS does not call `/api/admin/holidays` or `/api/admin/system-config`.

## Centralized API Layer

### `src/main/resources/static/js/api.js`
This file centralizes all API requests used by the frontend.
- Authorization header management via `ems_token`
- JSON and `FormData` support
- Generic request helpers: `get`, `post`, `put`, `delete`
- Explicit route functions for each feature area

Feature endpoints defined in `api.js`:
- Auth: login, forgotPassword, resetPassword
- Employees: list, detail, create, update, delete, upload photo, export Excel, export PDF
- Departments: list, detail, employees by department, create, update, delete
- Leaves: list, employee leaves, manager leaves, leave balances, apply, approve/reject, accrue, policies, policy update
- Shifts: list, create
- Profile changes: list/my-requests, pending, submit, approve, reject
- Expenses: list, employee claims, manager claims, submit, approve, reject
- Attendance: check-in, check-out, today, history
- Payroll: history, period query, generate, pay, payslip
- Performance: employee reviews, add review
- Dashboard: stats

## Page / Component to Backend Endpoint Matrix

| Page / Component | Frontend File(s) | Backend Endpoint(s) | Status |
|---|---|---|---|
| Login page | `src/main/resources/static/index.html`, `src/main/resources/static/js/auth.js` | `POST /api/auth/login` | Implemented |
| Forgot password | `src/main/resources/static/index.html`, `src/main/resources/static/js/auth.js` | `POST /api/auth/forgot-password` | Implemented |
| Reset password | `src/main/resources/static/index.html`, `src/main/resources/static/js/auth.js` | `POST /api/auth/reset-password` | Implemented |
| Dashboard stats | `src/main/resources/static/dashboard.html`, `src/main/resources/static/js/app.js` | `GET /api/dashboard/stats` | Implemented |
| Employee directory | `dashboard.html`, `app.js`, `api.js` | `GET /api/employees`, `GET /api/employees/{id}`, `POST /api/employees`, `PUT /api/employees/{id}`, `DELETE /api/employees/{id}`, `POST /api/employees/{id}/photo` | Implemented |
| Employee export | `dashboard.html`, `app.js`, `api.js` | `GET /api/employees/export/excel`, `GET /api/employees/export/pdf` | Implemented |
| Department management | `dashboard.html`, `app.js`, `api.js` | `GET /api/departments`, `GET /api/departments/{id}`, `GET /api/departments/{id}/employees`, `POST /api/departments`, `PUT /api/departments/{id}`, `DELETE /api/departments/{id}` | Implemented |
| Leave management | `dashboard.html`, `app.js`, `api.js` | `GET /api/leaves`, `GET /api/leaves/employee/{employeeId}`, `GET /api/leaves/manager/{managerId}`, `GET /api/leaves/balances/employee/{employeeId}`, `POST /api/leaves/apply`, `POST /api/leaves/{id}/approve`, `POST /api/leaves/accrue`, `GET /api/leaves/policies`, `PUT /api/leaves/policies/{id}` | Implemented |
| Shift listing | `dashboard.html`, `app.js`, `api.js` | `GET /api/shifts`, `POST /api/shifts` | Implemented |
| Profile page | `dashboard.html`, `app.js`, `api.js` | `GET /api/employees/{employeeId}`, `GET /api/profile-changes/my-requests`, `POST /api/profile-changes`, `GET /api/profile-changes/pending`, `POST /api/profile-changes/{id}/approve`, `POST /api/profile-changes/{id}/reject` | Implemented |
| Expense claims | `dashboard.html`, `app.js`, `api.js` | `GET /api/expenses`, `GET /api/expenses/employee/{employeeId}`, `GET /api/expenses/manager/{managerId}`, `POST /api/expenses`, `POST /api/expenses/{id}/approve`, `POST /api/expenses/{id}/reject` | Implemented |
| Attendance | `dashboard.html`, `app.js`, `api.js` | `POST /api/attendance/check-in/{employeeId}`, `POST /api/attendance/check-out/{employeeId}`, `GET /api/attendance/today/{employeeId}`, `GET /api/attendance/history/{employeeId}` | Implemented |
| Payroll | `dashboard.html`, `app.js`, `api.js` | `GET /api/payroll/history/{employeeId}`, `GET /api/payroll/period`, `POST /api/payroll/generate`, `POST /api/payroll/{id}/pay`, `GET /api/payroll/{id}/payslip` | Implemented |
| Performance reviews | `dashboard.html`, `app.js`, `api.js` | `GET /api/performance/employee/{employeeId}`, `POST /api/performance` | Implemented |
| Notifications | `dashboard.html`, `app.js`, `api.js` | `GET /api/notifications/subscribe` | Implemented |
| Admin user management | `admin-dashboard.html`, `js/admin.js` | `GET /api/admin/users`, `POST /api/admin/users/{id}/deactivate`, `POST /api/admin/users/{id}/reactivate`, `PUT /api/admin/users/{id}/role` | Implemented |
| Admin exports | `admin-dashboard.html`, `js/admin.js` | `GET /api/admin/export/audit-logs`, `GET /api/admin/export/company-directory` | Implemented |
| Admin holidays / system config | `admin-dashboard.html`, `js/admin.js` | `/api/admin/holidays`, `/api/admin/system-config` | Not wired in current UI |

## Unmapped Backend Endpoints

These backend routes exist in controllers but have no current frontend call in the static UI pages found:
- `GET /api/admin/audit-logs` (AuditLogController)
- `PUT /api/admin/holidays/{id}` (HolidayController)
- `DELETE /api/admin/holidays/{id}` (HolidayController)
- `PUT /api/admin/system-config/{id}` (SystemConfigController)
- `DELETE /api/admin/system-config/{id}` (SystemConfigController)

## Notes and Next Steps

- `src/main/resources/static/js/api.js` is the central contract for frontend/backend communication. Any new UI feature should add a function here and then use it from the page script.
- The admin UI currently does not use holiday or system configuration endpoints. If the admin console is meant to manage those resources, add client-side calls and UI wiring in `admin-dashboard.html` and `src/main/resources/static/js/admin.js`.
- If more endpoint detail is needed, the controller classes under `src/main/java/com/ems/controller` are the authoritative backend route definitions.
