# Enterprise Employee Management System (EMS) - Architecture & File Structure

This document provides a comprehensive overview of the Enterprise Employee Management System architecture, tracing its data flow from entry point to output, and demystifying the directory hierarchy.

## Directory Structure & Purposes

The system is built on a modern, decoupled **Client-Server Architecture** utilizing stateless RESTful APIs. The repository structure maps out the two main services:

* **`frontend/`**: The root of the React presentation application.
  * **[package.json](file:///a:/My%20project/employee-management-system/frontend/package.json)**: Manages the React 19, MUI, and Vite dependencies.
  * **`src/components/`, `layouts/`, `pages/`**: Contains the scalable interface elements. Views are broken down into logical domains (`employees`, `auth`, `attendance`, `leaves`).
  * **`src/context/`**: Contains `AuthContext.tsx` which houses the JWT parsing and global user state.
  * **`src/routes/`**: Contains [AppRoutes.tsx](file:///a:/My%20project/employee-management-system/frontend/src/routes/AppRoutes.tsx) which maps paths to pages and `ProtectedRoute.tsx` which stops unauthorized traversal entirely using the context store.

* **`src/main/java/com/ems/...`**: Contains the entire backend Spring Boot app.
  * **`config/`**: Configurations for CORS security, generic app bean creation, and Security Filter Chains.
  * **`controller/`**: The API entry points (e.g., `EmployeeController`, `AttendanceController`). Responsible for processing HTTP mappings (`@GetMapping`, `@PostMapping`).
  * **`service/`**: The business logic layer. Abstracted away from the controllers, it computes logic (like cron calculation or leave policy enforcement) and dictates transactional boundaries.
  * **`repository/`**: Spring Data JPA interfaces representing queries to the MySQL database.
  * **`model/`**: The JPA Entities (e.g., `Employee`, `User`, `LeaveRequest`) representing the SQL tables and their schemas relations.
  * **`dto/` & `mapper/`**: Data Transfer Objects ensure that database Entities aren't leaked to the client directly. Mappers handle the translation between Entity strings and DTOs.

* **`docker-compose.yml` / `Dockerfile`**: Responsible for containerization configurations mapping ports `8080` (backend) and `5173` (frontend), bridging them to the database.

---

## Data Flow Walkthrough (From Entry to Output)

To explain how components fit together, let’s trace the data flow for typical actions—such as retrieving a list of Employees. 

**1. The Request Originates (Frontend / React)**  
A user (for example, an HR representative) logs in. The frontend receives a JWT in the response payload and saves it into local storage. When the user navigates to `/employees`, the `AppRoutes` mounts the `<EmployeeList />` page. The page triggers an `Axios` call: `GET /api/employees` and injects the `"Bearer <JWT_TOKEN>"` into the header.

**2. Authentication Filter (Backend / Spring Security)**  
The incoming HTTP request hits the Spring Security Filter Chain. The `JwtAuthenticationFilter` validates the signature of the token and checks if the token has expired. If valid, the user's roles (e.g., `ROLE_HR`) are placed into the `SecurityContext`.

**3. The Controller Layer (`EmployeeController.java`)**  
The request reaches the Spring Dispatcher Servlet and is mapped to `EmployeeController.getAllEmployees()`. The backend enforces the rules via SpEL:
```java
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER')")
```
If the user wasn't one of these roles, the backend immediately throws a `403 Forbidden` response.

**4. The Service Layer (`EmployeeService.java`)**  
The controller creates no logic itself. Instead, it passes the pagination options to `EmployeeService.getAllEmployees()`. The service calculates the logic, checks if the user requested a specific filtered list (like a department search), and delegates to the database. 

**5. The Repository Data Layer (`EmployeeRepository.java`)**  
The `EmployeeRepository` extends Spring Data `JpaRepository`. A Pageable query executes a translated SQL `SELECT * FROM employees` statement against the MySQL instance, mapping the raw rows into Java `Employee` Entities.

**6. The Transformation & Response**  
The service receives `Employee` entities. Before giving them back to the controller, the `Mapper` converts them into safe `EmployeeDTO` objects to strip out things like raw passwords or internal database IDs if necessary. The Controller parses this into JSON and sends a `200 OK` response back to the client.

**7. Presentation & Output**  
The React application's active `axios` call resolves. The `EmployeeList` page updates its state variable, and React re-renders the Data Grid table components on the screen displaying the fetched list of employees cleanly to the HR representative.

---

## 3-Level Hierarchy Folder Tree (Annotated)

```text
employee-management-system/ (Level 0 - Project Root)
├── .github/ (Level 1)
│   └── workflows/ (Level 2)
│       └── ci.yml (Level 3 - GitHub Actions configuration for automated build pipelines.) 
├── frontend/ (Level 1 - The React 19 Presentation Layer)
│   ├── public/ (Level 2)
│   │   └── vite.svg (Level 3 - Generic public asset files hosted without processing.)
│   ├── src/ (Level 2 - Core Frontend Application Code)
│   │   ├── components/ (Level 3 - Reusable, isolated UI pieces like inputs, cards, or loading spinners.)
│   │   ├── pages/ (Level 3 - Full React route view components like `Dashboard.tsx` or `Login.tsx`.)
│   │   ├── services/ (Level 3 - Axios HTTP client implementations tying frontend hooks to backend endpoints.)
│   │   ├── routes/ (Level 3 - Route declaration trees (like `AppRoutes.tsx`) and Role-Based Guards.)
│   │   ├── App.tsx (Level 3 - The foundational React component that wraps the application in Theme and Auth Providers.)
│   │   └── main.tsx (Level 3 - Vite application entry point bootstrapping React DOM.)
│   ├── package.json (Level 2 - Defines all Node.js dependencies, linting, and Vite build scripts.)
│   ├── vite.config.ts (Level 2 - Configuration file for Vite hot-reloading and typescript build processes.)
│   └── Dockerfile (Level 2 - Instructions to containerize the frontend layer utilizing an Nginx server.)
├── src/ (Level 1 - The Spring Boot 3.4 Application Layer)
│   ├── main/ (Level 2 - Production Source Code)
│   │   ├── java/ (Level 3 - Encompasses the `com.ems` Java packages holding backend Controllers, Services, and Models.)
│   │   └── resources/ (Level 3 - Contains `application.properties` configuring the MySQL URL, port settings, and static `schema.sql` data.)
│   └── test/ (Level 2 - Testing Suite)
│       └── java/ (Level 3 - Holds automated JUnit and Mockito test files for application reliability.)
├── pom.xml (Level 1 - Maven Build Manager configuration file containing all Java Spring dependencies.)
├── docker-compose.yml (Level 1 - Orchestration file specifying how backend, frontend, and MySQL Docker containers should run together.)
├── Dockerfile (Level 1 - Instructions to containerize the backend Maven Spring Boot instance with Java 21.)
├── .env (Level 1 - Local environment variables used by Docker Compose like DB credentials and JWT secrets.)
├── deployment_guide.md (Level 1 - Outlines instructions and best practices for deploying the EMS stack to a production environment.)
└── README.md (Level 1 - Comprehensive system documentation outlining features, technologies, API endpoints, and startup instructions.)
```
