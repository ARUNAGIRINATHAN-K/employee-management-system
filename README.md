# Employee Management System (EMS)

A modern, full-stack Employee Management System featuring a premium glassmorphic UI, robust role-based access control (RBAC), and automated workflow engines.

---

## Project Summary

The Employee Management System (EMS) is designed to streamline day-to-day organizational operations. It handles core HR processes, shift rostering, attendance checking, leave accruals, and monthly payroll processing. The application features a sleek dark/light mode toggle with interactive micro-animations.

---

## Tech Stack

*   **Backend:** Java 26, Spring Boot 3.3, Spring Security, Spring Data JPA.
*   **Database:** MySQL (run locally or via XAMPP).
*   **Frontend:** Vanilla HTML5, CSS3 (Glassmorphism layout, CSS variables for dark/light themes), and vanilla JavaScript.
*   **Reporting:** Apache POI (Excel export), OpenPDF (Payslip and directory PDF reports).

---

## Repository & Code Structure

The project follows a standard Maven directory layout:

```text
employee-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/ems/
│   │   │   ├── config/            # SecurityConfig, WebMvcConfig, and DataSeeder
│   │   │   ├── controller/        # REST controllers (Auth, Employee, Leaves, Shift, Expenses, etc.)
│   │   │   ├── dto/               # Login, Password, and request transfer models
│   │   │   ├── entity/            # JPA entities (Employee, Leave, Shift, ExpenseClaim, User, etc.)
│   │   │   ├── filter/            # JWT validation filter
│   │   │   ├── repository/        # Spring Data JPA repositories
│   │   │   ├── security/          # UserDetails and JwtUtils helpers
│   │   │   └── service/           # Service layer implementation
│   │   └── resources/
│   │       ├── static/            # Static assets (HTML, CSS, JS, uploaded files)
│   │       │   ├── css/           # Glassmorphic stylesheet (style.css)
│   │       │   ├── js/            # Client scripts (auth.js, api.js, app.js)
│   │       │   ├── uploads/       # Profile photos storage
│   │       │   ├── dashboard.html # Main dashboard app interface
│   │       │   └── index.html     # Login page
│   │       └── application.properties
│   └── test/                      # Spring Boot JUnit tests
├── pom.xml                        # Maven dependency configuration
└── README.md                      # Project documentation
```

---

## Primary Users & Goals

### 1. HR Admins (`ROLE_HR`)
*   **Goal:** Maintain organization hierarchy, configure settings, and handle financial payouts.
*   **Capabilities:** Full CRUD on employees/departments, create and configure work shifts, define leave policies, trigger or schedule monthly leave accruals, review profile change requests, approve expense claims, and generate payslips.

### 2. Managers (`ROLE_MANAGER`)
*   **Goal:** Supervise department teams and process operational requests.
*   **Capabilities:** View department employees, track attendance logs, approve/reject leave applications, and review pending expense claims.

### 3. Employees (`ROLE_EMPLOYEE`)
*   **Goal:** Profile self-service, daily work logging, and compensation tracking.
*   **Capabilities:** Submit profile change requests (First/Last name, Phone), check in/out of assigned shifts (with automated grace period tracking), apply for leave, submit expense claims for reimbursement, view performance reviews, and download PDF payslips.

---

## Current Known Issues & Implementation Notes

1.  **Lombok Compatibility:** To avoid compilation issues with compiler versions on Java 26 (specifically `TypeTag` errors), **Lombok is not used** in this repository. All entity classes and DTOs utilize standard Java constructors, getters, setters, and builder subclasses. Do not add Lombok annotations to new classes.
2.  **Shift Crossing Days:** Standard attendance check-in/out records map to the same calendar date. Multi-day shifts spanning past midnight compare check-out times against standard end hours of the shift mapped to the start date.
3.  **Local Photo Uploads:** Uploaded photos are stored inside a local folder mapped to `uploads/`. Ensure write permissions are granted to the application directory.

---

## Getting Started & Contribution

### Prerequisites
*   Java JDK 26
*   Maven 3.x
*   MySQL Server (port 3306)

### Installation
1.  Configure the database credentials in [application.properties](file:///a:/My%20project/employee-management-system/src/main/resources/application.properties):
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/ems_db?createDatabaseIfNotExist=true
    spring.datasource.username=root
    spring.datasource.password=yourpassword
    ```
2.  Seed initial database values. The seeder is automated and runs on first launch to create shifts, departments, administrators, and leave policies.

### Run Tests
To verify code changes and run tests, execute:
```cmd
./mvnw.cmd clean test
```

### Run Locally
Launch the Spring Boot development server:
```cmd
./mvnw.cmd spring-boot:run
```
The application will be accessible at: **[http://localhost:8080](http://localhost:8080)**.

### Seeding Credentials
On initial startup, log in with the following default accounts:
*   **HR Admin:** `admin` / `admin123`
*   **Manager:** `manager` / `manager123`
*   **Employee:** `employee` / `employee123`
