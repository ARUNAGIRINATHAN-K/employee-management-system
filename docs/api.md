# API Reference

All backend requests should target the API path prefixed with `/api`.

---

## Authentication & Authorization

All endpoints except `POST /api/auth/login` require an `Authorization` header populated with a valid JWT token.

```http
Authorization: Bearer <JWT_TOKEN_HERE>
```

---

## Authentication Endpoints (`/api/auth`)

### 1. User Login
* **Method**: `POST`
* **Path**: `/api/auth/login`
* **Authentication**: None (Public)
* **Request Payload**:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
* **Response Payload (200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlkIjoxLCJyb2xlcyI6WyJST0xFX0FETUlOIl19...",
    "type": "Bearer",
    "id": 1,
    "username": "admin",
    "email": "admin@ems.com",
    "roles": ["ROLE_ADMIN"]
  }
  ```

### 2. User Account Registration
* **Method**: `POST`
* **Path**: `/api/auth/register`
* **Authentication**: `ROLE_ADMIN`
* **Request Payload**:
  ```json
  {
    "username": "johndoe",
    "email": "john.doe@company.com",
    "password": "SecurePassword123",
    "roles": ["ROLE_EMPLOYEE"]
  }
  ```
* **Response Payload (210 Created)**:
  ```json
  {
    "message": "User registered successfully"
  }
  ```

### 3. List User Accounts
* **Method**: `GET`
* **Path**: `/api/auth/users`
* **Authentication**: `ROLE_ADMIN`
* **Query Parameters**:
  * `search` (Optional string): Match usernames/emails
  * `page` (Default: `0`), `size` (Default: `10`), `sort` (Default: `createdAt,desc`)
* **Response Payload (200 OK)**:
  ```json
  {
    "content": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@ems.com",
        "roles": ["ROLE_ADMIN"],
        "linkedEmployeeId": null,
        "linkedEmployeeName": null,
        "createdAt": "2026-06-26T12:00:00Z",
        "updatedAt": "2026-06-26T12:00:00Z"
      }
    ],
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0
  }
  ```

### 4. Admin Reset Password
* **Method**: `PUT`
* **Path**: `/api/auth/users/{id}/password`
* **Authentication**: `ROLE_ADMIN`
* **Request Payload**:
  ```json
  {
    "newPassword": "NewStrongPassword789"
  }
  ```
* **Response Payload (200 OK)**:
  ```json
  {
    "message": "Password updated successfully"
  }
  ```

### 5. Delete User Account
* **Method**: `DELETE`
* **Path**: `/api/auth/users/{id}`
* **Authentication**: `ROLE_ADMIN`
* **Response Status**: `204 No Content`

---

## Employee Endpoints (`/api/employees`)

### 1. Create Employee Profile
* **Method**: `POST`
* **Path**: `/api/employees`
* **Authentication**: `ROLE_ADMIN`, `ROLE_HR`
* **Request Payload**:
  ```json
  {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@company.com",
    "phone": "+1234567890",
    "hireDate": "2026-06-01",
    "jobTitle": "Software Engineer",
    "status": "ACTIVE",
    "departmentId": 2
  }
  ```
* **Response Payload (201 Created)**: Returns the saved employee details.

### 2. List Employees
* **Method**: `GET`
* **Path**: `/api/employees`
* **Authentication**: `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER`
* **Query Parameters**:
  * `search` (Optional string): Search by names or emails
  * `departmentId` (Optional long): Filter by department ID
  * `status` (Optional string): Filter by status (`ACTIVE`, `INACTIVE`, `ON_LEAVE`)
  * `page`, `size`, `sort`
* **Response Payload (200 OK)**: Returns standard Spring Data Page payload containing array of employee objects.

### 3. Assign Login Account directly to Employee
* **Method**: `POST`
* **Path**: `/api/employees/{id}/account`
* **Authentication**: `ROLE_ADMIN`
* **Request Payload**:
  ```json
  {
    "username": "johndoe",
    "password": "password123",
    "role": "ROLE_EMPLOYEE"
  }
  ```
* **Response Payload (200 OK)**: Returns the updated employee payload including newly linked user information.

---

## Department Endpoints (`/api/departments`)

### 1. List Departments
* **Method**: `GET`
* **Path**: `/api/departments`
* **Authentication**: All authenticated roles
* **Response Payload (200 OK)**: Paginated lists of department objects.

### 2. Dropdown List
* **Method**: `GET`
* **Path**: `/api/departments/list`
* **Authentication**: All authenticated roles
* **Response Payload (200 OK)**: Flat JSON list (array) of departments.

---

## Dashboard Stats Endpoint (`/api/dashboard`)

### 1. Fetch KPI Metrics
* **Method**: `GET`
* **Path**: `/api/dashboard/stats`
* **Authentication**: `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER`
* **Response Payload (200 OK)**:
  ```json
  {
    "totalEmployees": 18,
    "activeEmployees": 15,
    "onLeaveEmployees": 2,
    "terminatedEmployees": 1,
    "totalDepartments": 4,
    "departmentDistributions": {
      "Engineering": 8,
      "Human Resources": 3,
      "Sales": 5,
      "Management": 2
    }
  }
  ```

---

## Frontend Client Routes & Page Components Map

Below is the complete route-by-route map of the user interface screens, the exact file implementations, the allowed roles, and the API endpoints consumed by each component:

| Route Path | React Component | Source File | Allowed Role(s) | Consumes API Endpoint(s) | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/login` | `Login` | [Login.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/auth/Login.tsx) | *Public* | `POST /api/auth/login` | Simple user login interface. Saves JWT token to local storage and updates `AuthContext`. |
| `/dashboard` | `Dashboard` | [Dashboard.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/dashboard/Dashboard.tsx) | `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER` | `GET /api/dashboard/stats` | Renders dashboard metrics, active/on-leave summaries, and department distribution bars. |
| `/employees` | `EmployeeList` | [EmployeeList.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/employees/EmployeeList.tsx) | `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER` | `GET /api/employees` | Searchable, filterable, and paginated grid of employees. Supports delete prompts for admins. |
| `/employees/new` | `EmployeeForm` | [EmployeeForm.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/employees/EmployeeForm.tsx) | `ROLE_ADMIN`, `ROLE_HR` | `POST /api/employees` | Form to create a new employee profile. Includes client-side valid constraints. |
| `/employees/:id` | `EmployeeDetails`| [EmployeeDetails.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/employees/EmployeeDetails.tsx) | `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER` | `GET /api/employees/{id}`, `POST /api/employees/{id}/account` | Renders complete employee details and houses the "Set Login Account" workflow interface. |
| `/employees/:id/edit`| `EmployeeForm` | [EmployeeForm.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/employees/EmployeeForm.tsx) | `ROLE_ADMIN`, `ROLE_HR` | `GET /api/employees/{id}`, `PUT /api/employees/{id}` | Edit form for updating general employee profile fields. |
| `/departments` | `DepartmentList` | [DepartmentList.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/departments/DepartmentList.tsx) | `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER` | `GET /api/departments` | Displays a paginated list of all departments and active employee counts. |
| `/departments/new` | `DepartmentForm` | [DepartmentForm.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/departments/DepartmentForm.tsx) | `ROLE_ADMIN` | `POST /api/departments` | UI to add a new company department. |
| `/departments/:id` | `DepartmentDetails`| [DepartmentDetails.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/departments/DepartmentDetails.tsx)| `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER` | `GET /api/departments/{id}` | Shows details of a department and a list of all assigned employees. |
| `/departments/:id/edit`| `DepartmentForm`| [DepartmentForm.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/departments/DepartmentForm.tsx) | `ROLE_ADMIN` | `GET /api/departments/{id}`, `PUT /api/departments/{id}` | Form to modify department name, code, or description. |
| `/users` | `UserList` | [UserList.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/users/UserList.tsx) | `ROLE_ADMIN` | `GET /api/auth/users`, `DELETE /api/auth/users/{id}`, `PUT /api/auth/users/{id}/password` | Paginated user management table. Allows password resetting and account deletion. |
| `/users/new` | `CreateUserForm` | [CreateUserForm.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/users/CreateUserForm.tsx) | `ROLE_ADMIN` | `POST /api/auth/register` | Admin-facing console to register a free-standing user account with custom roles. |
| `/profile` | `Profile` | [Profile.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/profile/Profile.tsx) | *Authenticated* | `GET /api/employees/user/{userId}` | Employee self-service page. Fetches and displays their own mapped details. |
| `/unauthorized` | `Unauthorized` | [Unauthorized.tsx](file:///a:/My%20project/employee-management-system/frontend/src/pages/Unauthorized.tsx)| *Authenticated* | None | Error screen rendered when a user attempts to access an unauthorized route. |

