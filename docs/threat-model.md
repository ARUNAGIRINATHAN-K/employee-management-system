# Threat Model

This document identifies potential security threats targeting the Employee Management System, categorizes risk vectors, and details the technical controls implemented to mitigate them.

---

## Threat Classification (STRIDE)

The system threat vectors are evaluated against the STRIDE methodology:

| Threat | Risk Vector | Mitigation Control | Status |
| :--- | :--- | :--- | :--- |
| **Spoofing** | Unauthorized entity logging in as valid user or admin. | Stateless JWT signature verification; BCrypt strong password hashing. | **Mitigated** |
| **Tampering** | Modifying HTTP request payload to manipulate databases or bypass validation. | Strict DTO binding, `@Valid` constraint validation on models, Hibernate parameterization. | **Mitigated** |
| **Repudiation** | User performing critical actions without audit trace. | Service level logging; unique constraint keys on database tables. | **Partially Mitigated** |
| **Information Disclosure** | Data leaks via unauthorized access or raw error traces. | Custom global exception handler returning sanitised error messages; SpEL authorization checks. | **Mitigated** |
| **Denial of Service** | Flooding backend or brute-forcing endpoints. | Pageable size limits (max 100), Docker resource allocation limits. | **Partially Mitigated** |
| **Elevation of Privilege** | Normal user calling Admin or HR endpoints. | Method security via `@PreAuthorize` and custom SpEL expression evaluation bean. | **Mitigated** |

---

## Detailed Risk Assessments & Controls

### 1. Privilege Escalation (User vs. Employee Decoupling)
* **Threat**: A regular employee (`ROLE_EMPLOYEE`) attempts to fetch data belonging to another employee by modifying the request URL parameter (`/api/employees/{id}`).
* **Technical Control**: The `@sec.isOwnerOrPrivileged(#id)` expression checks the security context. If the authenticated principal does not match the database-linked employee ID of the target request, Spring Security issues a `403 Forbidden` response prior to service invocation.

### 2. Administrator Lockout & Self-Deletion Safeguards
* **Threat**: An administrator accidentally deletes their own account, or deletes the last admin account, locking the enterprise out of management facilities.
* **Technical Control**: `AuthServiceImpl.java` checks:
  1. If the target user ID matches the logged-in administrator's identity. If true, the system throws a `ResponseStatusException` (Bad Request) preventing self-deletion.
  2. If the user to delete has the `ROLE_ADMIN` role, the system queries the database to ensure `countByRole("ROLE_ADMIN") > 1`. If it is the last admin account, deletion is blocked.

### 3. Password Integrity & Brute Force Vulnerabilities
* **Threat**: Adversaries compromise the database and extract user credentials, or execute online dictionary attacks.
* **Technical Control**: 
  * Passwords are encrypted using BCrypt. Direct SQL injection of passwords is not possible due to parameterized query generation inside JPA/Hibernate.
  * *Future Recommendation*: Implement an account lockout policy (e.g. locks account for 15 minutes after 5 failed login attempts) in the user security layer.

### 4. Cross-Site Scripting (XSS) & Cross-Site Request Forgery (CSRF)
* **Threat**: Script injection into employee fields, or malicious sites executing requests on behalf of authenticated clients.
* **Technical Control**:
  * Frontend variables are handled securely using React's virtual DOM, escaping dangerous HTML characters during interpolation.
  * CSRF risk is mitigated by using stateless JWTs passed within client request headers (e.g., `Authorization`) rather than implicit browser cookies. This blocks cross-origin requests from executing authentication payloads automatically.

---

## Recommended Operational Controls

To transition the deployment into production, the following security hardening steps are recommended:

1. **HTTPS Traffic Enforcement**: Route all traffic through an Nginx proxy or cloud load-balancer terminated with a valid SSL/TLS certificate.
2. **Environment Secret Separation**: Replace default application secrets (`ems.jwt.secret`) and database credentials with cloud-managed environment values injected at start. Do not check production `.env` files into source control.
3. **Database Network Isolation**: Bind the MySQL instance to the internal Docker network only (`localhost` or private virtual network), ensuring database access is isolated from public internet queries.
