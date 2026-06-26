# System Architecture

This document provides a technical overview of the Employee Management System (EMS) architecture, database relationships, and request lifecycle flows.

---

## Architecture Overview

The system is designed following a decoupled, three-tier architecture patterns (Presentation, Application, and Data tiers) packaged as Docker containers.

```mermaid
graph TD
    subgraph Client Tier [Presentation Layer]
        Browser[Web Browser]
        ReactSPA[React SPA / TypeScript]
        MUI[Material-UI / Outfit & Inter Fonts]
        ReactSPA -->|HTTP / JSON + JWT| API_Gateway
    end

    subgraph Backend Tier [Application Layer]
        API_Gateway[Nginx Router / Ingress]
        SpringSecurity[Spring Security Filter Chain]
        JWTAuth[JWT JWTAuthenticationFilter]
        Controllers[REST Controllers]
        Services[Business Logic Service Layer]
        SecurityUtils[SpEL Security Expression Bean]
        
        API_Gateway -->|Forward| SpringSecurity
        SpringSecurity --> JWTAuth
        JWTAuth -->|Validate Claims| Controllers
        Controllers -.->|Access Control via SpEL| SecurityUtils
        Controllers --> Services
    end

    subgraph Database Tier [Data Layer]
        Hibernate[Spring Data JPA / Hibernate]
        MySQL[(MySQL Database)]
        
        Services --> Hibernate
        Hibernate -->|SQL Queries| MySQL
    end

    style Client Tier fill:#f9f9f9,stroke:#333,stroke-width:1px
    style Backend Tier fill:#fcfcfc,stroke:#333,stroke-width:1px
    style Database Tier fill:#f6f6f6,stroke:#333,stroke-width:1px
```

### Components

1. **Presentation Layer (Frontend)**:
   * Built as a Single Page Application (SPA) using React, TypeScript, and Vite.
   * Styled according to the **Minimalism UI specification** using Material-UI (MUI), prioritizing a clean, typography-focused layout (using Outfit for headers and Inter for body text) with flat containers, no heavy borders, and zero gradients.
   * Manages authentication state client-side using a React Context Provider (`AuthContext`), persisting the JWT in local storage.

2. **Application Layer (Backend)**:
   * Built with Spring Boot (Java 17).
   * Employs **Spring Security** with stateless JWT authentication for securing REST endpoints.
   * Employs method-level security (`@PreAuthorize`) with a custom Spring Bean (`@sec`) evaluating SpEL expressions for fine-grained resource ownership checks.
   * Implements Data Transfer Objects (DTOs) for strict control over request and response formats.

3. **Data Layer (Database)**:
   * Relational database using MySQL.
   * Object-Relational Mapping (ORM) powered by Spring Data JPA and Hibernate.
   * Automated schema migrations mapped via JPA annotations and initialized via SQL seed scripts (`schema.sql`, `data.sql`).

---

## Detailed Request Lifecycle Flow

Below is the request execution sequence for an authenticated API call requesting protected employee data.

```mermaid
sequenceDiagram
    autonumber
    actor User as Client (React App)
    participant SecFilter as Spring Security Filter Chain
    participant JWTProvider as JwtAuthenticationFilter
    participant Controller as EmployeeController
    participant SecBean as SecurityUtils (@sec)
    participant Service as EmployeeService
    participant DB as MySQL DB

    User->>SecFilter: GET /api/employees/12 (Header: Authorization: Bearer <JWT>)
    SecFilter->>JWTProvider: Intercept and extract JWT
    JWTProvider->>JWTProvider: Validate signature, expiration & claims
    alt Token is Valid
        JWTProvider-->>SecFilter: Set SecurityContextHolder Authentication
    else Token is Invalid / Expired
        JWTProvider-->>User: 401 Unauthorized Response
    end
    SecFilter->>Controller: Route to getEmployeeById(12)
    Controller->>SecBean: Evaluate @sec.isOwnerOrPrivileged(12)
    SecBean->>SecBean: Check roles (ADMIN/HR/MANAGER) or compare current token userId with employee's userId
    alt User is Authorized
        SecBean-->>Controller: Access Granted
    else User is Forbidden
        SecBean-->>User: 403 Forbidden Response
    end
    Controller->>Service: getEmployeeById(12)
    Service->>DB: Fetch Employee Entity + User details
    DB-->>Service: Employee Entity Record
    Service->>Service: Map Entity to EmployeeDTO
    Service-->>Controller: EmployeeDTO
    Controller-->>User: 200 OK (JSON Payload)
```

---

## Database Model Schema

The entity relationship diagram highlights the database schema links between employees, departments, users, and roles:

```mermaid
erDiagram
    DEPARTMENT {
        Long id PK
        String name UK
        String description
        String code UK
    }
    EMPLOYEE {
        Long id PK
        String first_name
        String last_name
        String email UK
        String phone
        Date hire_date
        String job_title
        String status
        Long department_id FK
        Long user_id FK "Nullable"
    }
    USER {
        Long id PK
        String username UK
        String email UK
        String password "BCrypt Hash"
        Boolean enabled
        DateTime created_at
        DateTime updated_at
    }
    ROLE {
        Long id PK
        String name UK "ROLE_ADMIN, ROLE_HR, ROLE_MANAGER, ROLE_EMPLOYEE"
    }
    USER_ROLES {
        Long user_id FK
        Long role_id FK
    }

    DEPARTMENT ||--o{ EMPLOYEE : "has"
    USER ||--o| EMPLOYEE : "associated_with"
    USER ||--o{ USER_ROLES : "possesses"
    ROLE ||--o{ USER_ROLES : "granted_to"
```

### Architectural Key Points
* **One-to-One Linkage**: The `Employee` entity maintains a nullable `@OneToOne` join column to `User`. This decouples standard administrative employee entry from user account activation.
* **Cascades & Deletions**: Deleting a `User` account sets the `user_id` FK on the corresponding `Employee` to null, preserving historical employee records while terminating login capabilities.
* **Role Association**: Roles are managed as a separate table (`role`) linked via a join table (`user_roles`) supporting multi-role configuration, though standard setups assign single primary business roles (`ROLE_ADMIN`, `ROLE_HR`, `ROLE_MANAGER`, `ROLE_EMPLOYEE`).
