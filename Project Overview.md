# Project Overview - Employee Management System (EMS)

### Project basics
*   **Title:** Employee Management System [EMS]
*   **Short description:** A modern enterprise portal to streamline shift scheduling, profile management, automated leave accruals, and payroll processing, featuring a responsive, premium glassmorphic UI.

---

## Table of Contents
1.  [Overview](#overview)
2.  [Architecture Summary](#architecture-summary)
3.  [Roles & Permissions](#roles--permissions)
4.  [User-Wise Features](#user-wise-features)
5.  [User Flow](#user-flow)
6.  [Page-Wise Flow & Wireframes](#page-wise-flow--wireframes)
7.  [API & Data Model](#api--data-model)
8.  [Security & Privacy](#security--privacy)
9.  [Testing & QA](#testing--qa)
10. [Monitoring & Observability](#monitoring--observability)
11. [Deployment & CI/CD](#deployment--cicd)
12. [Roadmap & Milestones](#roadmap--milestones)
13. [Onboarding & Contribution Guide](#onboarding--contribution-guide)
14. [Appendix](#appendix)

---

## Overview

### Project purpose and goals
*   Simplify workforce management by automating leaves, shift rostering, and payroll additions.
*   Provide a premium, interactive user experience to employees and HR managers.
*   Ensure transactional integrity across automated routines (e.g., leave allocations, payslip generations).

### Problem statement
*   Manual leave calculations and hardcoded payroll variables lead to payroll discrepancies and human errors.
*   Lack of shift-based attendance rules makes it difficult to track late check-ins and early departures.
*   Paper-based profile updates or expense claim submissions create administrative bottlenecks.

### Target users and stakeholders
*   **HR Admins:** Manage organization policies, shifts, payroll directories, and approve claims/updates.
*   **Managers:** Monitor department attendance, review leave requests, and approve expenses.
*   **Regular Employees:** Track daily attendance, submit profile updates, request leave, and upload expense receipts.

### High-level success metrics
*   Reduce monthly payroll generation time to under [5 minutes].
*   Decrease leave accrual calculations error rate to [0.0%].
*   Ensure [100%] audit log tracking for sensitive administrative actions.

---

## Architecture Summary

### System components
*   **Frontend UI:** Single Page App structure built with Vanilla HTML, CSS (Glassmorphism design variables), and Vanilla JS (`app.js`, `api.js`).
*   **Backend Server:** Spring Boot 3.3 application containing REST Controllers, Services, and Repositories.
*   **Database:** Local/XAMPP MySQL database storing employee, shift, leave, attendance, and expense records.
*   **Reporting Engine:** OpenPDF for PDF payslip generation and Apache POI for Excel employee directories.

### Deployment and hosting
*   **Application Server:** Runs on [Spring Boot embedded Tomcat container on port 8080].
*   **Database Hosting:** Maintained on local [MySQL server instance].
*   **Resource Storage:** Dynamic assets (e.g., employee photo uploads) stored in a local directory [uploads/].

### Data flow diagram
```text
   +-------------+       REST API       +----------------+       Hibernate       +----------+
   |  Glassmorph | <------------------> |  Spring Boot   | <-------------------> |  MySQL   |
   |  Frontend   |      (JSON/JWT)      |  REST Backend  |        (JPA)          |  Database|
   +-------------+                      +----------------+                       +----------+
          ^                                     |
          | Photo Uploads                       v
          +----------------------------> [Local Storage]
```

### Tech stack
*   **Backend:** Java 26, Spring Boot 3.3.4, Spring Security, Hibernate ORM, OpenPDF, Apache POI.
*   **Frontend:** HTML5, CSS3, Vanilla JS, Chart.js.
*   **Build System:** Apache Maven (with `mvnw.cmd` wrapper).

---

## Roles & Permissions

### Role: ROLE_HR
*   **Responsibilities:** Full control over employee directory, shifts, leave policies, and organizational payouts.
*   **Allowed actions:**
    *   Create, Read, Update, Delete (CRUD) on `Employee` records.
    *   CRUD on `Department` and `Shift` records.
    *   Read and Update on `LeavePolicy` configuration.
    *   Approve/Reject profile updates, leave requests, and expense claims.
    *   Generate and trigger `Payroll` runs.
*   **Access restrictions:** None. Can view all records in the organization.

### Role: ROLE_MANAGER
*   **Responsibilities:** Oversight of assigned department team members and processing operational requests.
*   **Allowed actions:**
    *   Read access to department `Employee` listings.
    *   Approve/Reject leave applications and expense claims submitted by direct reports.
    *   Read access to attendance logs of direct reports.
*   **Access restrictions:** Restricted from modifying basic salary, custom allowances, leave policies, or running organization payroll.

### Role: ROLE_EMPLOYEE
*   **Responsibilities:** Log daily work metrics, maintain personal profile correctness, and apply for benefits.
*   **Allowed actions:**
    *   Read personal `Employee` profile.
    *   Submit `ProfileChangeRequest` for personal fields (First Name, Last Name, Phone).
    *   Check-in and Check-out attendance logs.
    *   Submit `Leave` applications and `ExpenseClaim` reimbursements.
    *   Read own `LeaveBalance`, `Attendance` history, and download personal payslips.
*   **Access restrictions:** Restricted from viewing other employees' salary information, approving own requests, or editing department details.

---

## User-wise features

### Persona: HR Admin
*   **Core features:** Dynamic shift creation, employee payroll configuration, manual leave accrual triggers.
*   **Edge-case behaviors:** Re-generating payroll locks processed expense claims as `PAID` to prevent double payout.
*   **UI pages/screens:** Employee Directory modal, Shifts modal, Leave Policies table, Change Requests porównanie panel, Payroll generate modal.

### Persona: Team Manager
*   **Core features:** Approving expense claims, processing team leave applications.
*   **Edge-case behaviors:** Attempting to process already-approved leaves throws a "Leave request already processed" exception.
*   **UI pages/screens:** Leaves approval queue, Pending Expenses table, Department details overview.

### Persona: Regular Employee
*   **Core features:** Profile Change Request submission, shift attendance logging, expense claim filing.
*   **Edge-case behaviors:** Late check-ins compare local system time against shift start time plus the configured grace period minutes.
*   **UI pages/screens:** My Profile tab, Submit Expense Claim modal, Attendance history, Leave application form.

---

## User flow

### Primary Flow: Submit & Approve Profile Change
*   **Preconditions:** Employee is authenticated; has active profile.
*   **Flow Steps:**
    1.  Employee opens "My Profile" and clicks "Request Profile Edit".
    2.  Employee fills updated First/Last Name and Phone and clicks Submit.
    3.  Backend saves pending `ProfileChangeRequest` record.
    4.  HR Admin logs in, navigates to "Change Requests", and views the side-by-side comparison of old vs new values.
    5.  HR clicks Approve; backend applies JSON changes to `Employee` entity and sets status to `APPROVED`.
*   **Postconditions:** Employee entity reflects the updated values; request status is locked.
*   **Alternative Flow / Error Handling:** If HR clicks Reject, a modal requests comments. Request status updates to `REJECTED` with remarks.

### Primary Flow: Expense Claim Reimbursement via Payroll
*   **Preconditions:** Employee has active claim; manager is authenticated.
*   **Flow Steps:**
    1.  Employee submits expense claim with category and amount.
    2.  Manager clicks Approve in "Pending Expense Approvals" table.
    3.  HR generates payroll for the month. Backend aggregates all `APPROVED` claims for that month.
    4.  The aggregated sum is added to the payroll's `allowances` field.
    5.  Claims are marked as `PAID` in a single transaction.
*   **Postconditions:** Generated payroll contains expense values; claims status becomes `PAID`.

---

## Page-wise flow / Wireframes

### Page: My Profile (`/dashboard.html` -> `#sectionProfile`)
*   **Purpose:** Displays current employee records and lets them request updates.
*   **Key UI elements:** Glassmorphic avatar, personal information grid, request edit button, request history table.
*   **Data shown and user actions:** Displays email, phone, department, shift, salary, and allowances. User can trigger the edit modal.
*   **Layout wireframe:**
```text
+-------------------------------------------------------------+
| [Avatar]                   Personal Info Details            |
| User Name                  - Email: [email]   - Shift: [name] |
| Job Title                  - Phone: [phone]   - Salary: $[xx] |
| <Request Edit Button>                                       |
+-------------------------------------------------------------+
| My Profile Change Requests                                  |
| ID | Submitted At | Requested Changes | Status   | Comments |
| 1  | 2026-05-23   | Phone: 123->456   | APPROVED | Checked  |
+-------------------------------------------------------------+
```

### Page: Change Requests Dashboard (`/dashboard.html` -> `#sectionChangeRequests`)
*   **Purpose:** Side-by-side comparison panel for HR to approve profile changes.
*   **Key UI elements:** Dual column tables showing current value vs requested value.
*   **Data shown and user actions:** List of pending request cards. Actions: [Approve], [Reject].
*   **Layout wireframe:**
```text
+-------------------------------------------------------------+
| ID | Employee      | Changes Requested             | Action |
| 2  | Rahul Kumar   | Phone: [9876...] -> [9090...] | [Appr] |
|    | (ID: 3)       | Name: Rahul -> Rohit          | [Rejc] |
+-------------------------------------------------------------+
```

---

## API & data model

### Key API endpoints
*   `POST /api/profile-changes` - Submits a profile update request (Authenticated).
*   `GET /api/profile-changes/pending` - Lists pending requests (HR only).
*   `POST /api/profile-changes/{id}/approve` - Approves and applies changes (HR only).
*   `GET /api/leaves/policies` - Lists active leave policies (Authenticated).
*   `PUT /api/leaves/policies/{id}` - Updates policy rules (HR only).
*   `POST /api/expenses` - Submits an expense claim (Authenticated).
*   `POST /api/expenses/{id}/approve` - Approves expense claim (Manager/HR only).

### Database Entities
*   **Employee:** `id` (Long, PK), `first_name`, `last_name`, `email`, `phone`, `job_title`, `salary`, `status`, `shift_id`, `allowance_rate`, `deduction_rate`.
*   **Shift:** `id` (Long, PK), `name`, `start_time`, `end_time`, `grace_period_minutes`.
*   **LeavePolicy:** `id` (Long, PK), `leave_type` (Unique), `annual_allocation`, `monthly_accrual_rate`.
*   **ExpenseClaim:** `id` (Long, PK), `employee_id` (FK), `title`, `amount`, `category`, `status`, `claim_date`, `approved_by` (FK), `comments`.
*   **ProfileChangeRequest:** `id` (Long, PK), `employee_id` (FK), `requested_fields_json`, `status`, `submitted_at`, `processed_by` (FK), `comments`.

---

## Security & privacy

### Auth method
*   **JWT Authentication:** Stateless security filter extraction. Token carries username, role permissions, and user identifiers.
*   **Spring Security:** Custom UserDetailsService verifying DB records. Password hash using BCrypt encoder.

### Sensitive data handling and encryption
*   Passwords stored as BCrypt hashes in MySQL.
*   PII updates (First Name, Last Name, Phone) comparison dashboard keeps changes restricted to HR Admin users.
*   Financial details (basic salary, allowance rate) require HR credentials to access or modify.

### Rate limiting & abuse prevention
*   [Restricted login attempts: lock accounts after 5 failures].
*   JWT expiration configured to [24 hours] to minimize hijacking window.

---

## Testing & QA

### Test plans
*   **Unit Tests:** Verify payroll mathematical multipliers and shift grace threshold bounds.
*   **Integration Tests:** Verify database-driven leave policies initialize balances for new employee records.
*   **E2E Tests:** Simulate profile edit request submission, HR approval, and directories update.

### Critical test cases
*   Accrual does not double-allocate leaves for inactive or deleted employee records.
*   Generating payroll aggregates and pays approved claims, ignoring pending ones.
*   Submitting a shift with invalid times (e.g. start after end) is blocked by boundary validations.

---

## Monitoring & observability

### Observability structure
*   **Logs:** Console logs output Hibernate transactions and security filter events.
*   **Metrics:** Spring Boot Actuator reporting [JVM memory, thread pools, and active database connection counts].
*   **Errors:** Automated global exception handler mapping runtime exceptions into structured REST error responses.

### Success targets
*   Application Uptime: [99.9%].
*   REST API Response Time: P95 < [200ms].

---

## Deployment & CI/CD

### Build steps
1.  Run clean verify:
    ```cmd
    ./mvnw.cmd clean verify
    ```
2.  Package application runnable jar:
    ```cmd
    ./mvnw.cmd package -DskipTests
    ```

### Environments
*   **Development:** Local machines with H2 or local MySQL database instance.
*   **Staging:** [Remote test environment running identical schema configurations].
*   **Production:** [Cloud container platform deploying validated releases].

---

## Roadmap & milestones

### Roadmap milestones
*   **Short-term (Next 3 months):** Build mobile attendance geofencing checkpoints.
*   **Mid-term (3–9 months):** Develop multi-tier approval paths for large expense claim brackets.
*   **Long-term (9–18 months):** Create automated calendar integration for third-party calendars.

### Risks
*   Night shift cross-day boundary check-in records conflict with standard weekly analytics.
*   Concurrent payroll runs can trigger duplicate transactions if database isolations are weak.

---

## Onboarding & contribution guide

### Local execution
1.  Verify JDK 26 and MySQL installation.
2.  Open database terminal and create local schema: `CREATE DATABASE ems_db;`.
3.  Launch server using:
    ```cmd
    ./mvnw.cmd spring-boot:run
    ```
4.  Navigate to browser at `http://localhost:8080` and sign in.

### Branching & PRs
*   Branch name template: `feature/F-[id]-[name]` or `bugfix/B-[id]-[name]`.
*   PR requirement: must have [1] approval from project owner; must pass all local Maven test builds.

---

## Appendix

### Glossary
*   **Accrual:** Scheduled addition of leave allocations based on policy rates.
*   **Grace Period:** Allowable threshold minutes after shift start time where a check-in is not marked late.
*   **Payslip:** PDF report of monthly payroll payments.

### References
*   [Spring Boot Documentation](https://spring.io/projects/spring-boot)
*   [OpenPDF GitHub Repository](https://github.com/LibrePDF/OpenPDF)
*   [Apache POI Documentation](https://poi.apache.org/)
