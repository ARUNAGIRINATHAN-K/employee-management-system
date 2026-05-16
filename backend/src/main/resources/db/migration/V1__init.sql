-- V1__init.sql

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_enabled BIT NOT NULL DEFAULT FALSE,
    is_account_non_locked BIT NOT NULL DEFAULT TRUE,
    failed_login_attempts INT DEFAULT 0,
    email_verified BIT NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(255),
    reset_password_token VARCHAR(255),
    refresh_token VARCHAR(512)
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_active BIT NOT NULL DEFAULT TRUE,
    head_id BIGINT
);

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    gender VARCHAR(255),
    date_of_birth DATE,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    designation VARCHAR(100),
    joining_date DATE NOT NULL,
    termination_date DATE,
    status VARCHAR(255) NOT NULL,
    salary DECIMAL(12,2),
    profile_image VARCHAR(255),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    bank_account_number VARCHAR(30),
    bank_name VARCHAR(100),
    tax_id VARCHAR(30),
    shift_start VARCHAR(255),
    shift_end VARCHAR(255),
    user_id BIGINT,
    department_id BIGINT,
    manager_id BIGINT,
    CONSTRAINT fk_emp_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_emp_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_emp_mgr FOREIGN KEY (manager_id) REFERENCES employees(id)
);

ALTER TABLE departments ADD CONSTRAINT fk_dept_head FOREIGN KEY (head_id) REFERENCES employees(id);

CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time DATETIME,
    check_out_time DATETIME,
    status VARCHAR(255) NOT NULL,
    work_hours DOUBLE,
    overtime_hours DOUBLE,
    is_late BIT NOT NULL DEFAULT FALSE,
    late_minutes INT,
    notes VARCHAR(500),
    ip_address VARCHAR(50),
    CONSTRAINT uk_employee_date UNIQUE (employee_id, attendance_date),
    CONSTRAINT fk_att_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(255) NOT NULL,
    approved_by BIGINT,
    rejection_reason VARCHAR(500),
    total_days BIGINT,
    CONSTRAINT fk_lr_emp FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_lr_appr FOREIGN KEY (approved_by) REFERENCES employees(id)
);

CREATE TABLE payroll (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    employee_id BIGINT NOT NULL,
    pay_period_start DATE NOT NULL,
    pay_period_end DATE NOT NULL,
    basic_salary DECIMAL(12,2) NOT NULL,
    allowances DECIMAL(12,2) DEFAULT 0.00,
    deductions DECIMAL(12,2) DEFAULT 0.00,
    tax DECIMAL(12,2) DEFAULT 0.00,
    overtime_pay DECIMAL(12,2) DEFAULT 0.00,
    bonus DECIMAL(12,2) DEFAULT 0.00,
    net_salary DECIMAL(12,2),
    is_paid BIT NOT NULL DEFAULT FALSE,
    paid_date DATE,
    payment_method VARCHAR(50),
    transaction_reference VARCHAR(100),
    CONSTRAINT fk_pay_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(50),
    is_read BIT NOT NULL DEFAULT FALSE,
    link VARCHAR(500),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BIT NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    employee_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT,
    file_path VARCHAR(255) NOT NULL,
    document_type VARCHAR(50),
    description VARCHAR(500),
    CONSTRAINT fk_doc_emp FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Indices
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_dept_name ON departments(name);
CREATE INDEX idx_dept_code ON departments(code);
CREATE INDEX idx_emp_employee_id ON employees(employee_id);
CREATE INDEX idx_emp_email ON employees(email);
CREATE INDEX idx_emp_department ON employees(department_id);
CREATE INDEX idx_emp_status ON employees(status);
CREATE INDEX idx_emp_joining_date ON employees(joining_date);
CREATE INDEX idx_att_employee ON attendance(employee_id);
CREATE INDEX idx_att_date ON attendance(attendance_date);
CREATE INDEX idx_att_status ON attendance(status);
CREATE INDEX idx_lr_employee ON leave_requests(employee_id);
CREATE INDEX idx_lr_status ON leave_requests(status);
CREATE INDEX idx_lr_dates ON leave_requests(start_date, end_date);
CREATE INDEX idx_pay_employee ON payroll(employee_id);
CREATE INDEX idx_pay_period ON payroll(pay_period_start, pay_period_end);
CREATE INDEX idx_notif_user ON notifications(user_id);
CREATE INDEX idx_notif_read ON notifications(is_read);
CREATE INDEX idx_doc_employee ON documents(employee_id);

