-- ============================================================
-- Employee Management System - Database Schema
-- Database: MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS ems_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ems_db;

-- ============================================================
-- 1. ROLES
-- Lookup table for authorization roles.
-- Exists independently; referenced by the join table user_roles.
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)     NOT NULL,
    description VARCHAR(255)    NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. USERS
-- Stores login credentials. Each row is an authenticated
-- identity; the actual employee profile lives in `employees`.
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)     NOT NULL,
    email       VARCHAR(100)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email),
    INDEX idx_users_username (username),
    INDEX idx_users_email    (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. USER_ROLES (join / bridge table)
-- Models the Many-to-Many between Users and Roles.
-- A user can hold multiple roles; a role can be assigned to
-- multiple users.
-- ============================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id     BIGINT  NOT NULL,
    role_id     BIGINT  NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_user_roles_user_id (user_id),
    INDEX idx_user_roles_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. DEPARTMENTS
-- Organizational unit. Referenced by employees.
-- ============================================================
CREATE TABLE IF NOT EXISTS departments (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(255)    NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_departments_name UNIQUE (name),
    INDEX idx_departments_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. EMPLOYEES
-- Core business entity. Links to one department (Many-to-One)
-- and optionally to one user account (One-to-One).
-- ============================================================
CREATE TABLE IF NOT EXISTS employees (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    first_name      VARCHAR(50)     NOT NULL,
    last_name       VARCHAR(50)     NOT NULL,
    email           VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20)     NULL,
    job_title       VARCHAR(100)    NOT NULL,
    salary          DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    hire_date       DATE            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    department_id   BIGINT          NULL,
    user_id         BIGINT          NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uk_employees_email   UNIQUE (email),
    CONSTRAINT uk_employees_user_id UNIQUE (user_id),

    CONSTRAINT fk_employees_department
        FOREIGN KEY (department_id) REFERENCES departments (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT fk_employees_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_employees_email         (email),
    INDEX idx_employees_department_id (department_id),
    INDEX idx_employees_status        (status),
    INDEX idx_employees_last_name     (last_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SEED DATA - All Application Roles
-- ============================================================
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN',    'Full system access: manage employees, departments, users, and system settings'),
    ('ROLE_HR',       'HR access: full employee CRUD and department view only'),
    ('ROLE_MANAGER',  'Manager access: view employees in own department, read-only departments'),
    ('ROLE_EMPLOYEE', 'Employee access: view and update own profile only')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ============================================================
-- SEED DATA - Default Admin User (admin / admin123)
-- ============================================================
INSERT INTO users (id, username, email, password) VALUES
    (1, 'admin', 'admin@ems.com', '$2a$10$h8ZZJdAs1MIQNjCU.Kw73.ZtTmMxFFtDTUQJEN9hdzAgVZi5nd51O')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Map Admin User (user_id=1) to ROLE_ADMIN (role_id=1)
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1)
ON DUPLICATE KEY UPDATE role_id = role_id;


