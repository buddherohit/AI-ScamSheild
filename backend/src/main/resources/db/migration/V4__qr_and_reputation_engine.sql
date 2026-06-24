-- ==============================================================================
-- AI ScamShield - Phase 5 QR Intelligence & UPI Reputation Engine Schema (MySQL)
-- ==============================================================================

-- 1. Create UPI Profiles table
CREATE TABLE upi_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    upi_id VARCHAR(255) NOT NULL UNIQUE,
    normalized_upi VARCHAR(255) NOT NULL,
    risk_score INT NOT NULL DEFAULT 0,
    risk_level VARCHAR(50) NOT NULL DEFAULT 'SAFE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE INDEX idx_upi_profiles_normalized ON upi_profiles(normalized_upi);

-- 2. Create Merchant Profiles table
CREATE TABLE merchant_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    upi_profile_id BIGINT NOT NULL,
    merchant_name VARCHAR(255) NOT NULL,
    mcc VARCHAR(10),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    CONSTRAINT fk_merchant_profiles_upi FOREIGN KEY (upi_profile_id) REFERENCES upi_profiles(id) ON DELETE CASCADE
);

CREATE INDEX idx_merchant_profiles_upi ON merchant_profiles(upi_profile_id);

-- 3. Create QR Scans table
CREATE TABLE qr_scans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    raw_text TEXT NOT NULL,
    extracted_upi VARCHAR(255),
    extracted_merchant VARCHAR(255),
    risk_score INT,
    risk_level VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    CONSTRAINT fk_qr_scans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_qr_scans_user ON qr_scans(user_id);

-- 4. Create Verification Requests table
CREATE TABLE verification_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    ip_address VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL, -- 'UPI', 'QR'
    entity_value VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_verification_requests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_verification_requests_user ON verification_requests(user_id);

-- 5. Create Verification Results table
CREATE TABLE verification_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    risk_score INT NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    recommendation VARCHAR(255),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_verification_results_request FOREIGN KEY (request_id) REFERENCES verification_requests(id) ON DELETE CASCADE
);

CREATE INDEX idx_verification_results_request ON verification_results(request_id);

-- 6. Create Risk Reasons table
CREATE TABLE risk_reasons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL, -- 'UPI', 'QR'
    entity_value VARCHAR(255) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE INDEX idx_risk_reasons_entity ON risk_reasons(entity_type, entity_value);

-- 7. Create Reputation History table
CREATE TABLE reputation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    verified_entity VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL, -- 'UPI', 'QR'
    risk_score INT NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL, -- 'TRUSTED', 'SAFE', 'SUSPICIOUS', 'DANGEROUS'
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_reputation_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_reputation_history_user ON reputation_history(user_id);
