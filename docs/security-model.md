# Security Model

This document outlines the security architecture, authorization policy configurations, and cryptographic controls implemented in the Employee Management System.

---

## Role-Based Access Control (RBAC)

The system enforces a strict RBAC policy mapping system capabilities to user groups. The system registers four primary roles:
1. **ROLE_ADMIN**: Full system read/write capability, user management (account creation, deletion, password reset), and schema seeding operations.
2. **ROLE_HR**: Full employee lifecycle operations (create, update, view) and department management (read-only). Restricted from creating user accounts or updating security policy.
3. **ROLE_MANAGER**: High-level reporting view. Read access to lists of employees and departments, and metrics dashboards. No edit, deletion, or user management permissions.
4. **ROLE_EMPLOYEE**: Self-service only. Can read their own personal employee record, view directory listings (read-only departments), but has no other administrative accesses.

---

## API Permission Matrix

The REST API endpoints enforce permissions using method-level security. The mapping of endpoints to allowed roles is detailed below:

| HTTP Method | Route | Description | Required Role(s) | Custom Authorization Logic |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/login` | Authenticate user & return JWT | *Public* | None |
| **POST** | `/api/auth/register` | Register a new user | `ROLE_ADMIN` | None |
| **GET** | `/api/auth/users` | List user accounts (paginated) | `ROLE_ADMIN` | None |
| **PUT** | `/api/auth/users/{id}/password` | Reset password for a user | `ROLE_ADMIN` | Self-reset restrictions |
| **DELETE** | `/api/auth/users/{id}` | Delete user account | `ROLE_ADMIN` | Cannot self-delete, last-admin checks |
| **GET** | `/api/dashboard/stats` | Retrieve aggregated metrics | `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER` | None |
| **POST** | `/api/employees` | Add a new employee profile | `ROLE_ADMIN`, `ROLE_HR` | None |
| **GET** | `/api/employees` | List employee profiles (paginated) | `ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER` | None |
| **GET** | `/api/employees/{id}` | Retrieve specific employee details | *Authenticated* | `@sec.isOwnerOrPrivileged(#id)` |
| **GET** | `/api/employees/user/{userId}` | Retrieve employee by user ID | *Authenticated* | `@sec.isSelfOrPrivileged(#userId)` |
| **PUT** | `/api/employees/{id}` | Update employee profile data | `ROLE_ADMIN`, `ROLE_HR` | None |
| **DELETE** | `/api/employees/{id}` | Permanently delete employee record | `ROLE_ADMIN` | None |
| **POST** | `/api/employees/{id}/account` | Create and bind user to employee | `ROLE_ADMIN` | Atomically creates linked user |
| **POST** | `/api/departments` | Create new department | `ROLE_ADMIN` | None |
| **PUT** | `/api/departments/{id}` | Update department details | `ROLE_ADMIN` | None |
| **DELETE** | `/api/departments/{id}` | Delete department | `ROLE_ADMIN` | None |
| **GET** | `/api/departments` | List departments (paginated) | *Authenticated* | None |
| **GET** | `/api/departments/list` | Raw dropdown list of departments | *Authenticated* | None |

---

## Custom SpEL Authorization Engine

Standard role-based configuration is insufficient for self-service profiles (where an employee should only access their own data). To solve this, a custom Spring Security evaluation bean is declared as `@sec`:

* **`isOwnerOrPrivileged(employeeId)`**:
  * If the caller holds roles `ROLE_ADMIN`, `ROLE_HR`, or `ROLE_MANAGER`, access is instantly granted.
  * If the caller holds `ROLE_EMPLOYEE`, the system maps the current authenticated security principal username to the `User` record, resolves its linked `Employee` ID, and grants access *only if* the target `employeeId` matches the caller's ID.
* **`isSelfOrPrivileged(userId)`**:
  * Compares target `userId` against the authenticated principal ID, allowing non-admin users to inspect their own linked entity properties while blocking requests targeting other users.

---

## JWT Authentication Protocol

Authentication is stateless, powered by JSON Web Tokens (JWT).

1. **Authentication Filter**: `JwtAuthenticationFilter` intercepts all incoming requests. It parses the token from the HTTP header:
   ```http
   Authorization: Bearer <JWT_STRING>
   ```
2. **Signature Verification**: The token signature is validated against the application's private key (`ems.jwt.secret`) configured in application properties.
3. **Session Context Injection**: If validated, user details, authorities (roles), and custom claims are parsed. A `UsernamePasswordAuthenticationToken` is populated and registered within the Spring Security `SecurityContextHolder`.

---

## Data Security & Cryptography

* **Password Hashing**: Raw user passwords are never stored in the database. The system uses a standard `BCryptPasswordEncoder` applying a cryptographically random salt and stretching factor (default strength 10).
* **Cross-Origin Resource Sharing (CORS)**: Access control headers are strictly configured. CORS policies allow API calls only from pre-approved domains (e.g., frontend host `http://localhost:5173` or port `80`) and restrict allowed headers and HTTP verbs.
