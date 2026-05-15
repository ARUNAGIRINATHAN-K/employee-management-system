INSERT INTO roles (name, description) VALUES ('ROLE_ADMIN', 'Administrator'), ('ROLE_HR', 'Human Resources'), ('ROLE_MANAGER', 'Manager'), ('ROLE_EMPLOYEE', 'Employee');

-- Optional sample user
INSERT INTO users (username, password, email) VALUES ('admin', 'password', 'admin@example.com');

-- Assign admin role to sample user
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
