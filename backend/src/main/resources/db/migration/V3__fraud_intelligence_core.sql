-- ==============================================================================
-- AI ScamShield - Phase 4 Fraud Intelligence Core Schema (MySQL)
-- ==============================================================================

-- 1. Create Fraud Categories table
CREATE TABLE fraud_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- Pre-populate Fraud Categories
INSERT INTO fraud_categories (code, display_name, description, created_at, updated_at) VALUES
('QR_SCAM', 'QR Code Scam', 'Fraudulent QR codes designed to steal credentials or siphon funds.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('UPI_SCAM', 'UPI Payment Scam', 'Scams utilizing UPI IDs or fake payment requests.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('BANKING_FRAUD', 'Banking Fraud', 'Phishing or credentials harvesting targeting bank accounts.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('KYC_FRAUD', 'KYC Verification Fraud', 'Scams requesting KYC documents to steal identities.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('OTP_FRAUD', 'OTP Theft Fraud', 'Social engineering or malware designed to intercept OTP codes.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LOAN_SCAM', 'Instant Loan Scam', 'Fake loan apps demanding processing fees upfront.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('INVESTMENT_SCAM', 'Investment Scam', 'Ponzi schemes or fraudulent trading platforms promising high returns.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('JOB_SCAM', 'Fake Job Scam', 'Scams charging registration fees for non-existent employment.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('IMPERSONATION', 'Impersonation Fraud', 'Pretending to be government officers, relatives, or delivery services.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('UNKNOWN', 'Unknown Scam', 'Uncategorized or generic fraudulent activities.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Create Reported Entities table
CREATE TABLE reported_entities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    value VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    CONSTRAINT uq_reported_entities_type_value UNIQUE (type, value)
);

CREATE INDEX idx_reported_entities_type_value ON reported_entities(type, value);

-- 3. Create Fraud Reports table
CREATE TABLE fraud_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    CONSTRAINT fk_fraud_reports_entity FOREIGN KEY (entity_id) REFERENCES reported_entities(id) ON DELETE CASCADE,
    CONSTRAINT fk_fraud_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_fraud_reports_category FOREIGN KEY (category_id) REFERENCES fraud_categories(id) ON DELETE RESTRICT
);

CREATE INDEX idx_fraud_reports_entity ON fraud_reports(entity_id);
CREATE INDEX idx_fraud_reports_reporter ON fraud_reports(reporter_id);

-- 4. Create Threat Indicators table
CREATE TABLE threat_indicators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    value VARCHAR(255) NOT NULL UNIQUE,
    source VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE INDEX idx_threat_indicators_type_value ON threat_indicators(type, value);

-- 5. Create Fraud Rules table
CREATE TABLE fraud_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    weight INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

-- Pre-populate Fraud Rules
INSERT INTO fraud_rules (rule_key, name, description, weight, is_active, created_at, updated_at, created_by, updated_by) VALUES
('RULE_REPORT_COUNT_GT_5', 'Reported More Than 5 Times', 'Triggered when the entity has been reported in more than 5 distinct, approved reports.', 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
('RULE_REPORT_COUNT_GT_20', 'Reported More Than 20 Times', 'Triggered when the entity has been reported in more than 20 distinct, approved reports.', 40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
('RULE_HIGH_SEVERITY_REPORTS', 'High Severity Reports', 'Triggered if the entity is associated with at least one approved report of HIGH or CRITICAL severity.', 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
('RULE_RECENT_FRAUD_ACTIVITY', 'Recent Fraud Activity', 'Triggered if there is any approved report or active indicator created in the last 24 hours.', 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM');

-- 6. Create Risk Assessments table
CREATE TABLE risk_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_value VARCHAR(255) NOT NULL,
    risk_score INT NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    reasons TEXT,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL
);

CREATE INDEX idx_risk_assessments_entity ON risk_assessments(entity_type, entity_value);

-- 7. Create Audit Events table
CREATE TABLE audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50),
    details TEXT,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL
);

CREATE INDEX idx_audit_events_type ON audit_events(event_type);
