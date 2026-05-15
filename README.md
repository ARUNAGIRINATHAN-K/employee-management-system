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

**WorkForceHub is an enterprise employee management system.**

Use it to manage your workforce across recruitment, attendance, leave, and payroll. Built with Java 21 and Spring Boot 3 for reliability and performance.

### What You Can Do
- Manage employee records and department assignments
- Track daily attendance and work hours
- Process leave requests with approval workflows
- Calculate and manage payroll
- View real-time team analytics and dashboards
- Monitor all activity through audit logs

## Key Features

| Feature | What It Does |
|---------|-------------|
| **Authentication** | Secure login with JWT tokens, role-based access control, and password recovery |
| **Employee Management** | Add, update, and organize employees with profiles, documents, and salary details |
| **Attendance Tracking** | Track check-in/out times and calculate work hours automatically |
| **Leave Management** | Employees request leave, managers approve/reject, system tracks balances |
| **Payroll** | Calculate salaries with deductions and generate pay slips |
| **Dashboard** | View real-time charts, team statistics, and hiring trends |
| **Notifications** | Receive in-app and email alerts for approvals and updates |
| **Audit Logs** | Track all user actions and data changes for compliance |
| **Export** | Generate PDF reports and Excel exports

## Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| **Backend** | Java 21 + Spring Boot 3 | Modern, stable, enterprise-ready |
| **Database** | MySQL 8.0 | Reliable relational database |
| **Frontend** | Thymeleaf + Bootstrap 5 | Server-side templates with responsive design |
| **Security** | Spring Security + JWT | Token-based authentication and role management |
| **ORM** | Spring Data JPA + Hibernate | Simplified database access with automatic queries |
| **API Docs** | SpringDoc OpenAPI (Swagger) | Auto-generated interactive API documentation |
| **Deployment** | Docker + Docker Compose | Containerized, easy deployment anywhere |
| **Testing** | JUnit 5 + Mockito | Comprehensive unit and integration tests |

## Project Structure

```
com.workforcehub/
├── config/       — Security, database, and web configuration
├── controller/   — REST API and web page endpoints
├── dto/          — Request and response data transfer objects
├── entity/       — Database models and JPA entities
├── enums/        — Type-safe enumerations (roles, statuses, etc.)
├── exception/    — Global error handling
├── repository/   — Database query methods
├── security/     — JWT authentication and user details
├── service/      — Business logic layer
├── util/         — Helper utilities
└── validation/   — Custom input validators
```

## Quick Start

### Prerequisites

- Java 21 or later
- Maven 3.9+ (or use the included Maven Wrapper)
- MySQL 8.0+ (or Docker)
- Docker (optional, recommended)

### Option 1: Run with Docker (Recommended)

**Start all services in one command:**

```bash
cd backend
docker-compose up -d
```

Access the app at `http://localhost:8080`

### Option 2: Run Locally

**1. Start MySQL:**

```sql
CREATE DATABASE workforcehub;
```

**2. Configure the database connection:**

Set environment variables or edit `application.yml`:
```bash
export DB_HOST=localhost
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

**3. Build and start the app:**

**Windows (PowerShell):**
```powershell
cd "backend\workforcehub"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"
$env:Path += ";C:\Program Files\Java\jdk-26.0.1\bin"
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
cd backend/workforcehub
./mvnw spring-boot:run
```

**4. Log in:**

Open `http://localhost:8080` and use:
- Username: `admin`
- Password: `Admin@123`

### Maven Wrapper

You don't need Maven installed globally. The project includes a Maven Wrapper that downloads and runs Maven automatically.

Use `.\mvnw.cmd` (Windows) or `./mvnw` (macOS/Linux) instead of `mvn`.

## API Documentation

### View the API

**Interactive Swagger UI:** `http://localhost:8080/swagger-ui.html`

Test endpoints directly from the browser without writing code.

### Common Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/register` | Create new account |
| POST | `/api/v1/auth/refresh-token` | Refresh JWT token |
| GET | `/api/v1/employees` | List all employees |
| POST | `/api/v1/employees` | Create new employee |
| GET | `/api/v1/departments` | List departments |
| POST | `/api/v1/attendance/check-in/{id}` | Check in for the day |
| POST | `/api/v1/leave-requests/employee/{id}` | Request leave |
| GET | `/api/v1/dashboard` | Get dashboard data |

### Response Format

All endpoints return consistent JSON:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "timestamp": "2026-05-15T10:30:00"
}
```

## Database

### Tables

The system uses 11 core tables:

- `users` — User accounts and authentication
- `roles` — Role definitions (Admin, HR, Manager, Employee)
- `user_roles` — Maps users to roles
- `employees` — Employee records and details
- `departments` — Department organization
- `attendance` — Check-in/out records
- `leave_requests` — Leave applications and approvals
- `payroll` — Salary and payment records
- `documents` — Employee file uploads
- `audit_logs` — Activity tracking
- `notifications` — User notifications

### Relationships

```
Users ──── Roles ──── Employees ──── Departments
                        │
                        ├── Attendance
                        ├── LeaveRequests
                        ├── Payroll
                        └── Documents
                
AuditLogs (tracks all changes)
```

## Docker Deployment

### Quick Start

```bash
cd backend
docker-compose up -d
```

### Common Commands

```bash
# View container logs
docker-compose logs -f app

# Stop all containers
docker-compose down

# Rebuild after code changes
docker-compose up -d --build
```

### Docker Compose Note

Newer Docker versions ignore the `version:` attribute in `docker-compose.yml`. If you see a warning about an obsolete version, remove this line from the top:

```yaml
version: '3.9'  # ← Delete this line
```

The file works the same without it.

## Testing

### Run Tests

```bash
# Run all tests
mvn test

# Generate coverage report
mvn test jacoco:report

# View coverage results
open target/site/jacoco/index.html
```

Coverage reports show which code is tested and which needs more tests.

## Security

**Your data is protected by:**

- JWT tokens for API authentication
- BCrypt password hashing (strength 12)
- Role-based access control (ADMIN, HR, MANAGER, EMPLOYEE)
- Account lockout after 5 failed login attempts
- CORS protection against cross-origin attacks
- Input validation to prevent SQL injection
- XSS protection through template escaping

## Monitoring

The app provides built-in health checks and metrics:

```
Health: GET /actuator/health
Info:   GET /actuator/info
Metrics: GET /actuator/metrics
```

Use these endpoints to verify the app is running and collect performance data.

## Production Deployment

### Set Environment Variables

Configure these before deployment:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-database-host
export DB_USERNAME=your-database-user
export DB_PASSWORD=your-database-password
export JWT_SECRET=your-256-bit-secret-key
export MAIL_HOST=smtp.gmail.com
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-email-password
```

### Build Production JAR

```bash
mvn clean package -DskipTests -Pprod
```

This creates an executable JAR file in `target/`.

### Run the JAR

```bash
java -jar target/workforcehub-1.0.0.jar
```

### Next Steps for Production

- Use a reverse proxy (Nginx) to manage traffic
- Enable HTTPS with SSL certificates
- Set up automated backups for the MySQL database
- Configure logging to centralize logs from all instances
- Use a process manager (systemd, supervisor) to keep the app running

## What's Next (Roadmap)

This section identifies features and improvements for future releases.

### High Priority Features

**Backend:**
- Implement database migrations with Flyway or Liquibase instead of auto-DDL
- Complete the Payroll Service to calculate and process salary payments
- Add a Document Storage Service for employee file uploads (local or AWS S3)
- Build an Email Service for notifications and password resets

**Frontend:**
- Create Payroll page for HR to run monthly processing
- Add Document Management UI to the employee profile
- Build User Administration page for admins to manage system accounts

### Medium Priority Improvements

**Backend:**
- Add PDF and Excel export capabilities with Apache POI and iText
- Implement caching (Redis or Caffeine) for frequently accessed data
- Activate rate limiting to prevent brute force attacks

**Frontend:**
- Build Calendar UI for attendance tracking
- Add client-side form validation with JavaScript
- Implement toast notifications for better user feedback

### Low Priority Polish

**Backend:**
- Secure the `/actuator` monitoring endpoints
- Set up automated database backups
- Add comprehensive audit logging with Hibernate Envers

**Frontend:**
- Add dark mode toggle
- Support multiple languages (i18n)
- Optimize mobile responsiveness

## Support & Contributing

Have questions or found a bug?

- **Report issues** on GitHub Issues
- **Contribute** by submitting pull requests
- **Discuss** ideas in Discussions tab

## License

MIT License — See LICENSE file for details

---

Built with ❤️ using Spring Boot 3 and Bootstrap 5
