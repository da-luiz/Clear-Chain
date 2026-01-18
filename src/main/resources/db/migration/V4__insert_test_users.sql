-- Insert default departments (these are safe to keep as they're just reference data)
-- Only insert if they don't exist to avoid conflicts
INSERT INTO departments (id, name, code, description, is_active, created_at) 
SELECT 1, 'IT', 'IT', 'Information Technology Department', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE code = 'IT');

INSERT INTO departments (id, name, code, description, is_active, created_at) 
SELECT 2, 'Human Resources', 'HR', 'Human Resources Department', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE code = 'HR');

INSERT INTO departments (id, name, code, description, is_active, created_at) 
SELECT 3, 'Finance', 'FINANCE', 'Finance and Accounting Department', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE code = 'FINANCE');

INSERT INTO departments (id, name, code, description, is_active, created_at) 
SELECT 4, 'Operations', 'OPERATIONS', 'Operations Department', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE code = 'OPERATIONS');

INSERT INTO departments (id, name, code, description, is_active, created_at) 
SELECT 5, 'Sales', 'SALES', 'Sales Department', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE code = 'SALES');

INSERT INTO departments (id, name, code, description, is_active, created_at) 
SELECT 6, 'Marketing', 'MARKETING', 'Marketing Department', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE code = 'MARKETING');

-- No users are created by default
-- The first admin user must be created through the registration/bootstrap endpoint
