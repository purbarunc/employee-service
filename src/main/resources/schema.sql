-- schema.sql: create tables for Employee and Address (PostgreSQL)

CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    salary DOUBLE PRECISION,
    hire_date DATE,
    status VARCHAR(50),
    manager VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS addresses (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id) ON DELETE CASCADE,
    street VARCHAR(255) NOT NULL,
    suite VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(50) NOT NULL,
    country VARCHAR(100),
    address_type VARCHAR(50) NOT NULL,
    is_primary BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_addresses_employee_id ON addresses(employee_id);
