-- Anvaya-Prajna-AI Core Database Schema
-- Version 1.0

CREATE TABLE IF NOT EXISTS explanation (
    id UUID PRIMARY KEY,
    question_id VARCHAR(64) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    difficulty VARCHAR(32),
    language VARCHAR(16) DEFAULT 'en',
    schema_version VARCHAR(16) NOT NULL DEFAULT '1.0',
    content_json TEXT NOT NULL,
    created_by VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_explanation_question_id ON explanation(question_id);
CREATE INDEX IF NOT EXISTS idx_explanation_status ON explanation(status);

CREATE TABLE IF NOT EXISTS explanation_step (
    id UUID PRIMARY KEY,
    explanation_id UUID NOT NULL REFERENCES explanation(id) ON DELETE CASCADE,
    sequence INTEGER NOT NULL,
    step_type VARCHAR(32) NOT NULL,
    content_json TEXT NOT NULL,
    validation_status VARCHAR(32) DEFAULT 'VALID'
);

CREATE INDEX IF NOT EXISTS idx_step_explanation_id ON explanation_step(explanation_id);

CREATE TABLE IF NOT EXISTS explanation_validation (
    id UUID PRIMARY KEY,
    explanation_id UUID NOT NULL REFERENCES explanation(id) ON DELETE CASCADE,
    validator VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    score DOUBLE PRECISION,
    details_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_validation_explanation_id ON explanation_validation(explanation_id);

CREATE TABLE IF NOT EXISTS explanation_feedback (
    id UUID PRIMARY KEY,
    explanation_id UUID NOT NULL REFERENCES explanation(id) ON DELETE CASCADE,
    step_id UUID,
    user_id VARCHAR(64),
    feedback_type VARCHAR(32) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_feedback_explanation_id ON explanation_feedback(explanation_id);

CREATE TABLE IF NOT EXISTS explanation_template (
    id UUID PRIMARY KEY,
    domain VARCHAR(64) NOT NULL,
    template_type VARCHAR(64) NOT NULL,
    content_json TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'
);
