-- ==============================================================================
-- AI ScamShield - Database Initial Schema Migration (MySQL)
-- ==============================================================================

-- Create User Accounts Table
CREATE TABLE users (
    id VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    roles VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- Index for fast user searches
CREATE INDEX idx_users_email ON users(email);

-- Insert Default Administrator Account
-- Password: password123 (BCrypt hash)
INSERT INTO users (id, email, password, name, roles, enabled, created_at, updated_at, created_by, updated_by)
VALUES (
    'usr_admin_001',
    'admin@scamshield.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qP.ee.L8U9tD2QzNmsxM.r4PqPsnVjD26W',
    'Enterprise Admin',
    'ROLE_ADMIN,ROLE_USER',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    'SYSTEM'
);
