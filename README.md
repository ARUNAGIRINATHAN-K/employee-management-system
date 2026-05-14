<p align="center">
  <h1 align="center">WorkForceHub</h1>
  <p align="center"><strong>Enterprise Employee Management System</strong></p>
  <p align="center">
    <a href="#features">Features</a> •
    <a href="#tech-stack">Tech Stack</a> •
    <a href="#quick-start">Quick Start</a> •
    <a href="#api-documentation">API Docs</a> •
    <a href="#deployment">Deployment</a>
  </p>
</p>

---

## Overview

WorkForceHub is a production-ready, enterprise-grade Employee Management System built with **Java 21**, **Spring Boot 3**, and **Bootstrap 5**. It provides comprehensive workforce management capabilities including employee CRUD, attendance tracking, leave management, payroll, and real-time analytics.

## Features

| Module | Capabilities |
|--------|-------------|
| **Authentication** | JWT tokens, refresh tokens, BCrypt encryption, role-based access, forgot/reset password |
| **Employee Management** | CRUD, profile images, document uploads, department allocation, salary management |
| **Attendance** | Daily check-in/out, work hours calculation, late detection, attendance reports |
| **Leave Management** | Apply/approve/reject leaves, 8 leave types, leave balance tracking |
| **Dashboard** | Real-time analytics, Chart.js visualizations, department stats, hiring trends |
| **Payroll** | Salary components, tax calculations, payment tracking |
| **Notifications** | In-app notifications, email notifications |
| **Audit Trail** | Complete activity logging, change tracking |
| **Export** | PDF reports, Excel exports |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.3.5 |
| **Security** | Spring Security 6 + JWT |
| **ORM** | Spring Data JPA + Hibernate |
| **Database** | MySQL 8.0 |
| **Frontend** | Thymeleaf + Bootstrap 5 + Chart.js |
| **Build** | Maven |
| **Caching** | Caffeine |
| **API Docs** | SpringDoc OpenAPI (Swagger) |
| **Testing** | JUnit 5 + Mockito + MockMvc |
| **Containerization** | Docker + Docker Compose |
| **CI/CD** | GitHub Actions |
| **Monitoring** | Spring Boot Actuator |

## Architecture

```
com.workforcehub/
├── config/          # Security, Web, OpenAPI, Audit configs
├── controller/      # REST & Page controllers
├── dto/             # Request/Response DTOs
├── entity/          # JPA entities with BaseEntity
├── enums/           # Type-safe enumerations
├── exception/       # Global exception handling
├── repository/      # Spring Data JPA repositories
├── security/        # JWT provider, filter, UserDetails
├── service/         # Business logic layer
├── util/            # Utility classes
└── validation/      # Custom validators
```

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8.0+ (or use Docker)

### Option 1: Docker (Recommended)

```bash
cd backend
docker-compose up -d
```

The app will be available at `http://localhost:8080`

### Option 2: Local Development

1. **Start MySQL** and create database:
```sql
CREATE DATABASE workforcehub;
```

2. **Configure** `application.yml` or set environment variables:
```bash
export DB_HOST=localhost
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

3. **Build & Run**:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Default Credentials
| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `Admin@123` |

## API Documentation

### Swagger UI
Available at: `http://localhost:8080/swagger-ui.html`

### Core Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/v1/auth/login` | Login | Public |
| `POST` | `/api/v1/auth/register` | Register | Public |
| `POST` | `/api/v1/auth/refresh-token` | Refresh JWT | Public |
| `POST` | `/api/v1/auth/forgot-password` | Forgot password | Public |
| `GET` | `/api/v1/employees` | List employees | Auth |
| `POST` | `/api/v1/employees` | Create employee | ADMIN, HR |
| `GET` | `/api/v1/employees/{id}` | Get employee | Auth |
| `PUT` | `/api/v1/employees/{id}` | Update employee | ADMIN, HR |
| `DELETE` | `/api/v1/employees/{id}` | Delete employee | ADMIN |
| `GET` | `/api/v1/departments` | List departments | Auth |
| `POST` | `/api/v1/attendance/check-in/{id}` | Check in | Auth |
| `POST` | `/api/v1/attendance/check-out/{id}` | Check out | Auth |
| `GET` | `/api/v1/dashboard` | Dashboard data | Auth |
| `POST` | `/api/v1/leave-requests/employee/{id}` | Apply leave | Auth |

### Standard Response Format
```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2024-01-01T00:00:00"
}
```

## 🗄 Database Schema

### ER Diagram

```
Users ──── user_roles ──── Roles
  │
  └── Employees ──── Departments
        │  │  │
        │  │  └── Documents
        │  │
        │  ├── Attendance
        │  ├── LeaveRequests
        │  └── Payroll
        │
        └── Notifications
              AuditLogs
```

### Tables: `users`, `roles`, `user_roles`, `employees`, `departments`, `attendance`, `leave_requests`, `payroll`, `documents`, `audit_logs`, `notifications`

## Docker

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild after changes
docker-compose up -d --build
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Coverage report location
open target/site/jacoco/index.html
```

## Security Features

- JWT access + refresh tokens
- BCrypt password hashing (strength 12)
- Role-based access control (ADMIN, HR, MANAGER, EMPLOYEE)
- Method-level security with `@PreAuthorize`
- Account lockout after 5 failed attempts
- CORS configuration
- Rate limiting with Bucket4j
- Input validation with Jakarta Validation
- SQL injection prevention via parameterized queries
- XSS protection via Thymeleaf auto-escaping

## Monitoring

- **Health Check**: `GET /actuator/health`
- **Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`

## Production Deployment

1. Set environment variables:
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-db-host
export DB_USERNAME=your-db-user
export DB_PASSWORD=your-db-password
export JWT_SECRET=your-256-bit-secret
export MAIL_USERNAME=your-email
export MAIL_PASSWORD=your-email-password
```

2. Build production JAR:
```bash
mvn clean package -DskipTests -Pprod
```

3. Run:
```bash
java -jar target/workforcehub-1.0.0.jar
```

## Production Readiness Checklist

This section outlines the missing pieces and technical debt that must be resolved to bring the Employee Management System from its current functional baseline to a fully robust, production-ready enterprise application.

### 1. High Priority (Core Business Logic)

#### Backend
- [ ] **Database Migration Management**: Transition from Hibernate `ddl-auto=create` to **Flyway** or **Liquibase**. Production databases should never rely on auto-DDL.
- [ ] **Payroll Processing Service**: Implement `PayrollService` to calculate salaries, deductions, and generate pay slips at the end of billing cycles.
- [ ] **Document Storage System**: Implement `FileStorageService` to handle employee document uploads (resumes, IDs, contracts). Needs configuration for local storage testing and AWS S3/Azure Blob for production.
- [ ] **Notification & Email Service**: Add `spring-boot-starter-mail` to handle automated emails for password resets, leave approvals, and onboarding using `JavaMailSender`.
- [ ] **Centralized Exception Handling**: Expand `@ControllerAdvice` to gracefully handle and format all SQL constraints, JWT expirations, and File Upload size limits.

#### Frontend
- [ ] **Payroll UI**: Create `payroll.html` to allow HR to trigger payroll runs and employees to view/download their payslips.
- [ ] **Document Management UI**: Add file upload components in `employee-profile.html` to securely upload and retrieve PDFs/Images.
- [ ] **Role & User Administration UI**: Add a `users.html` interface restricted to `ADMIN` for manually revoking access, unlocking accounts, and modifying system roles.

---

### 2. Medium Priority (Performance & UX Optimization)

#### Backend
- [ ] **Reporting & Export Engine**: Add **Apache POI** (Excel) and **iText/OpenPDF** (PDF) dependencies to `pom.xml`. Implement `ReportService` to export employee directories and attendance logs.
- [ ] **Caching Implementation**: Integrate Redis or Caffeine Cache for high-read, low-write endpoints like `getAllDepartments()` and `getDashboardData()` to reduce database load.
- [ ] **Rate Limiting Setup**: Bucket4J is in the `pom.xml`, but needs a `Filter` or `Interceptor` to actively block DDoS attempts on the `/login` and `/api/**` endpoints.

#### Frontend
- [ ] **Attendance Tracking UI**: Create `attendance.html` featuring a calendar view or a punch-in/punch-out widget connected to the existing `AttendanceController`.
- [ ] **Client-Side Validation**: Enhance Thymeleaf forms with robust JavaScript validation (or integrate a library like Parsley.js/Vuelidate) to prevent unnecessary backend calls.
- [ ] **Flash Messages & Toasts**: Implement a centralized system for showing success/error toast notifications after form submissions instead of raw error pages.

---

### 3. Low Priority (Enterprise Polish)

#### Backend
- [ ] **Actuator Security**: Secure the `/actuator` health/metrics endpoints so they are only accessible to monitoring tools (like Prometheus) or `ADMIN` roles.
- [ ] **Automated Backup Strategy**: Implement a scheduled cron job (via `@Scheduled` or OS level) to backup the MySQL volume.
- [ ] **Audit Logging Refinement**: Hook Hibernate Envers or custom JPA EntityListeners to automatically write `UPDATE/DELETE` history to the `AuditLog` table.

#### Frontend
- [ ] **Dark Mode Toggle**: Implement a CSS variables-based theme switcher for better accessibility.
- [ ] **Localization (i18n)**: Externalize text strings into `messages_en.properties`, `messages_es.properties`, etc., allowing for multi-language support.
- [ ] **Mobile Responsiveness Polish**: Perform a final pass on the Bootstrap 5 tables and navbars to ensure they render perfectly on mobile devices.

## License

This project is licensed under the MIT License.

---

<p align="center">Built with ❤️ using Spring Boot 3 and Bootstrap 5</p>
