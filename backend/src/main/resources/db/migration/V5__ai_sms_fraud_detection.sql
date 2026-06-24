-- ==============================================================================
-- AI ScamShield - Phase 6 AI SMS Fraud Detection & Intelligence Engine (MySQL)
-- ==============================================================================

-- 1. Create SMS Analysis table
CREATE TABLE sms_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    sms_text TEXT NOT NULL,
    risk_score INT NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    summary TEXT NOT NULL,
    recommendation TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    CONSTRAINT fk_sms_analysis_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_sms_analysis_user ON sms_analysis(user_id);

-- 2. Create SMS Analysis Reasons table
CREATE TABLE sms_analysis_reasons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    CONSTRAINT fk_sms_analysis_reasons_analysis FOREIGN KEY (analysis_id) REFERENCES sms_analysis(id) ON DELETE CASCADE
);

CREATE INDEX idx_sms_analysis_reasons_analysis ON sms_analysis_reasons(analysis_id);

-- 3. Create AI Requests table
CREATE TABLE ai_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    prompt TEXT NOT NULL,
    created_at DATETIME NOT NULL
);

-- 4. Create AI Responses table
CREATE TABLE ai_responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    raw_response TEXT NOT NULL,
    tokens_used INT,
    latency_ms BIGINT,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_ai_responses_request FOREIGN KEY (request_id) REFERENCES ai_requests(id) ON DELETE CASCADE
);

CREATE INDEX idx_ai_responses_request ON ai_responses(request_id);

-- 5. Create Prompt Templates table
CREATE TABLE prompt_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_key VARCHAR(50) NOT NULL UNIQUE,
    template_value TEXT NOT NULL,
    description VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- 6. Create Fraud Patterns table
CREATE TABLE fraud_patterns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_key VARCHAR(50) NOT NULL UNIQUE,
    pattern_regex VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    weight INT NOT NULL,
    description VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- 7. Create Analysis History table
CREATE TABLE analysis_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    sms_text_preview VARCHAR(255) NOT NULL,
    risk_score INT NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_analysis_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_analysis_history_user ON analysis_history(user_id);

-- ==============================================================================
-- Pre-populate default prompt templates
-- ==============================================================================
INSERT INTO prompt_templates (template_key, template_value, description, created_at, updated_at)
VALUES
('SMS_SYSTEM_PROMPT', 'You are an AI SMS Fraud Detection Specialist. Analyze the provided SMS message for potential scams or fraud. Extract details and return ONLY a valid JSON object matching the following structure (do not include markdown wrapping or other text): {\"riskScore\": <0-100>, \"riskLevel\": \"<LOW|MEDIUM|HIGH|CRITICAL>\", \"category\": \"<BANKING_FRAUD|KYC_SCAM|OTP_SCAM|LOAN_SCAM|INVESTMENT_SCAM|JOB_SCAM|DELIVERY_SCAM|LOTTERY_SCAM|IMPERSONATION|UNKNOWN>\", \"summary\": \"<1-2 sentence explanation of the threat>\", \"indicators\": [\"<indicator1>\", \"<indicator2>\", ...], \"recommendation\": \"<actionable advice for the user>\"}', 'System instructions for SMS analysis', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SMS_USER_PROMPT', 'Analyze this SMS message:\n\"{{smsText}}\"\n\nPre-analysis rule engine findings:\n- Matched Indicators: {{matchedIndicators}}\n- Rule-based Risk Score Estimate: {{ruleScore}}', 'User template matching engine inputs', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==============================================================================
-- Pre-populate fraud patterns for rules-based scanner
-- ==============================================================================
INSERT INTO fraud_patterns (pattern_key, pattern_regex, category, weight, description, created_at, updated_at)
VALUES
('OTP', '(?i)(otp|one time password|verification code|verification pin)', 'OTP_SCAM', 30, 'OTP request detection', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('KYC', '(?i)(kyc|re-verify|reverify|update profile|verification link)', 'KYC_SCAM', 25, 'KYC verify request', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PAN', '(?i)(pan card|pan number|update pan|link pan)', 'KYC_SCAM', 20, 'PAN card update alert', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('BANK_DETAILS', '(?i)(debit card|credit card|bank account|account blocked|netbanking|login credentials)', 'BANKING_FRAUD', 30, 'Banking credentials collection', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('REWARD', '(?i)(reward points|cashback|gift card|claim prize|scratch card)', 'LOTTERY_SCAM', 15, 'Reward point hooks', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LOTTERY', '(?i)(lottery|won cash|lucky draw|kbc draw|claim money)', 'LOTTERY_SCAM', 20, 'Fake lotteries', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SUSPENSION', '(?i)(suspend|suspended|blocked|freeze|frozen|deactivate|deactivated)', 'BANKING_FRAUD', 25, 'Account threat/suspension warnings', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('URGENT', '(?i)(immediately|urgent|within 24 hours|expire|expires|hurry|last warning)', 'UNKNOWN', 15, 'High urgency triggers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('REMOTE_ACCESS', '(?i)(anydesk|teamviewer|rustdesk|remote access|download app|install app)', 'UNKNOWN', 35, 'Remote access request tools', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
