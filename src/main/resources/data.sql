-- data.sql: idempotent sample data for first run (PostgreSQL)
-- Inserts are conditional to avoid duplicate key errors on repeated startups.

-- Insert sample employees if they don't exist (use email as natural key)
INSERT INTO employees (first_name, last_name, email, phone_number, salary, hire_date, status, manager, created_at, updated_at)
SELECT 'Alice', 'Smith', 'alice.smith@example.com', '+1-555-0100', 75000.0, '2021-06-15', 'ACTIVE', 'Bob Manager', now(), now()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE email = 'alice.smith@example.com');

INSERT INTO employees (first_name, last_name, email, phone_number, salary, hire_date, status, manager, created_at, updated_at)
SELECT 'John', 'Doe', 'john.doe@example.com', '+1-555-0111', 65000.0, '2020-03-10', 'ACTIVE', 'Jane Manager', now(), now()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE email = 'john.doe@example.com');

-- Insert addresses for Alice if not already present
INSERT INTO addresses (employee_id, street, suite, city, state, zip_code, country, address_type, is_primary, created_at, updated_at)
SELECT e.id, '123 Main St', 'Apt 1', 'Springfield', 'IL', '62704', 'USA', 'HOME', TRUE, now(), now()
FROM employees e
WHERE e.email = 'alice.smith@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM addresses a WHERE a.employee_id = e.id AND a.street = '123 Main St' AND a.zip_code = '62704'
  );

INSERT INTO addresses (employee_id, street, suite, city, state, zip_code, country, address_type, is_primary, created_at, updated_at)
SELECT e.id, '500 Corporate Blvd', 'Suite 200', 'Springfield', 'IL', '62701', 'USA', 'WORK', FALSE, now(), now()
FROM employees e
WHERE e.email = 'alice.smith@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM addresses a WHERE a.employee_id = e.id AND a.street = '500 Corporate Blvd' AND a.zip_code = '62701'
  );

-- Insert an address for John if not present
INSERT INTO addresses (employee_id, street, suite, city, state, zip_code, country, address_type, is_primary, created_at, updated_at)
SELECT e.id, '42 Elm St', NULL, 'Gotham', 'NY', '10001', 'USA', 'HOME', TRUE, now(), now()
FROM employees e
WHERE e.email = 'john.doe@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM addresses a WHERE a.employee_id = e.id AND a.street = '42 Elm St' AND a.zip_code = '10001'
  );

-- Ensure sequences are set to current max(id) to avoid duplicate key situations
SELECT setval(pg_get_serial_sequence('employees','id'), (SELECT COALESCE(MAX(id), 1) FROM employees));
SELECT setval(pg_get_serial_sequence('addresses','id'), (SELECT COALESCE(MAX(id), 1) FROM addresses));