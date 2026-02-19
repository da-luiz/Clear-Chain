-- Departments
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    code VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- Users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(150) NOT NULL UNIQUE,
    first_name VARCHAR(150) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    email_value VARCHAR(320) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department_id BIGINT REFERENCES departments(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    password_hash VARCHAR(255),
    last_login TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Vendor Categories
CREATE TABLE IF NOT EXISTS vendor_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    code VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

-- Vendor Performance Criteria
CREATE TABLE IF NOT EXISTS vendor_performance_criteria (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    weight DOUBLE PRECISION NOT NULL,
    max_score INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    category_id BIGINT REFERENCES vendor_categories(id),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_vendor_perf_category ON vendor_performance_criteria(category_id);

-- Vendors
CREATE TABLE IF NOT EXISTS vendors (
    id BIGSERIAL PRIMARY KEY,
    vendor_code VARCHAR(100) NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),
    tax_id VARCHAR(100),
    email_value VARCHAR(320),
    phone VARCHAR(50),
    address_street VARCHAR(255),
    address_city VARCHAR(255),
    address_state VARCHAR(255),
    address_postal_code VARCHAR(50),
    address_country VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_CREATION',
    category_id BIGINT REFERENCES vendor_categories(id),
    website VARCHAR(255),
    description TEXT,
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_vendors_category ON vendors(category_id);
CREATE INDEX IF NOT EXISTS idx_vendors_status ON vendors(status);

-- Vendor Ratings
CREATE TABLE IF NOT EXISTS vendor_ratings (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    criteria_id BIGINT NOT NULL REFERENCES vendor_performance_criteria(id),
    score INTEGER NOT NULL,
    comments TEXT,
    evidence_url VARCHAR(500),
    rated_by_user_id BIGINT NOT NULL REFERENCES users(id),
    rating_period_start TIMESTAMP WITHOUT TIME ZONE,
    rating_period_end TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_vendor_ratings_vendor ON vendor_ratings(vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_ratings_criteria ON vendor_ratings(criteria_id);

-- Vendor Creation Requests
CREATE TABLE IF NOT EXISTS vendor_creation_requests (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    requesting_department_id BIGINT NOT NULL REFERENCES departments(id),
    requested_by_user_id BIGINT NOT NULL REFERENCES users(id),
    vendor_id BIGINT REFERENCES vendors(id),
    company_name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),
    business_justification TEXT,
    expected_contract_value DOUBLE PRECISION,
    rejection_reason TEXT,
    additional_info_required TEXT,
    reviewed_by_user_id BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_vendor_requests_status ON vendor_creation_requests(status);
CREATE INDEX IF NOT EXISTS idx_vendor_requests_department ON vendor_creation_requests(requesting_department_id);

-- Vendor Approvals
CREATE TABLE IF NOT EXISTS vendor_approvals (
    id BIGSERIAL PRIMARY KEY,
    vendor_creation_request_id BIGINT NOT NULL REFERENCES vendor_creation_requests(id),
    approver_id BIGINT NOT NULL REFERENCES users(id),
    approval_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    comments TEXT,
    approved_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_vendor_approvals_request ON vendor_approvals(vendor_creation_request_id);

-- Contracts
CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,
    contract_number VARCHAR(100) NOT NULL UNIQUE,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    contract_value NUMERIC(19,2),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    contract_type VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    document_url VARCHAR(500),
    terms_and_conditions TEXT,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id),
    approved_by_user_id BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITHOUT TIME ZONE,
    renewal_terms TEXT,
    termination_clause TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_contracts_vendor ON contracts(vendor_id);
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts(status);

-- Purchase Orders
CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(100) NOT NULL UNIQUE,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    description TEXT,
    total_amount NUMERIC(19,2) NOT NULL,
    order_date DATE NOT NULL,
    expected_delivery_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    approval_threshold NUMERIC(19,2),
    created_by_user_id BIGINT NOT NULL REFERENCES users(id),
    approved_by_user_id BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITHOUT TIME ZONE,
    rejection_reason TEXT,
    delivery_address TEXT,
    payment_terms VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_vendor ON purchase_orders(vendor_id);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_status ON purchase_orders(status);

