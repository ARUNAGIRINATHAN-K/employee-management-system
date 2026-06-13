# Employee Management System (EMS)

A modern HR portal for a full employee lifecycle: onboarding, attendance, leave management, payroll, and role-based access control.

## Overview

EMS is a Spring Boot-based employee management application with a polished glassmorphic UI and a lightweight SPA frontend. It supports:

- Employee directory management
- Shift scheduling and attendance logging
- Leave applications and approval workflows
- Expense claim submission and review
- Payroll generation and payslip export
- Role-based access for HR, managers, and employees

## Key Features

- Centralized HR dashboard built with vanilla HTML/CSS/JavaScript
- Secure authentication using JWT
- REST API backend with Spring Security and Spring Data JPA
- Embedded H2 database for local development
- PDF and Excel exports for payroll and reports
- Built-in admin workflows for approvals, audits, and policy configuration

## Technology Stack

- Java 26
- Spring Boot 3.3.4
- Spring Security
- Spring Data JPA
- Hibernate ORM
- Embedded H2 database (default)
- Vanilla JavaScript + Chart.js
- OpenPDF and Apache POI
- Maven build system

## Repository Structure

```text
employee-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/ems/
│   │   │   ├── config/            # Security and MVC configuration, seeding
│   │   │   ├── controller/        # REST controllers for auth, employees, leaves, payroll, etc.
│   │   │   ├── dto/               # Request and response DTOs
│   │   │   ├── entity/            # JPA entities and domain models
│   │   │   ├── exception/         # Custom exception handling
│   │   │   ├── filter/            # JWT authentication filter
│   │   │   ├── repository/        # Spring Data JPA repositories
│   │   │   ├── security/          # JWT utilities and user details services
│   │   │   └── service/           # Business logic services
│   │   └── resources/
│   │       ├── static/            # Frontend assets, JS, CSS, HTML pages
│   │       └── application.properties
│   └── test/                      # JUnit tests
├── pom.xml                        # Maven project definition
└── README.md                      # Project documentation
```

## Getting Started

### Prerequisites

- Java JDK 26
- Maven 3.x
- Optional: Docker for containerized deployment

### Run Locally

Start the application with:

```powershell
cd "a:\My project\employee-management-system"
./mvnw.cmd spring-boot:run
```

Open the app at:

- `http://localhost:8080`
- `http://localhost:8080/h2-console` for the embedded H2 console

### Run Tests

```powershell
./mvnw.cmd clean test
```

## Default Seeded Accounts

The initial seed database includes these sample users:

- **HR Admin:** `admin` / `admin123`
- **Manager:** `manager` / `manager123`
- **Employee:** `employee` / `employee123`

## User Roles

### `ROLE_HR`

HR users can manage employees, departments, shifts, leave policies, payroll, expense approvals, and profile change requests.

### `ROLE_MANAGER`

Managers can review team attendance, approve or reject leave applications, and manage expense claim workflows for direct reports.

### `ROLE_EMPLOYEE`

Employees can log attendance, submit leave requests, request profile updates, and view payslips.

## Development Notes

- The app uses an embedded H2 database by default for quick local development.
- Static UI files are served from `src/main/resources/static/`.
- JWT tokens are issued by the backend and consumed by frontend requests.
- The seed data loader automatically creates initial users and basic lookup data.

## Deployment

### Docker (optional)

Build and run with Docker:

```powershell
docker build -t employee-management-system .
docker run --rm -p 8080:8080 employee-management-system
```

### Configuration

Database and environment settings are configured in `src/main/resources/application.properties`.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Run tests locally
4. Open a pull request with a clear summary of your changes

## License

This project is available under the terms of the [MIT License](LICENSE).
