-- V1__init_schema.sql
-- Flyway migration for Banco de Tempo (Escambo de Serviços e Habilidades)

CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    time_balance NUMERIC(10, 2) NOT NULL DEFAULT 2.00,
    blocked_balance NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    reputation_score DOUBLE PRECISION NOT NULL DEFAULT 5.00,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE skill_offers (
    id UUID PRIMARY KEY,
    provider_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    hourly_rate_factor NUMERIC(5, 2) NOT NULL DEFAULT 1.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exchange_proposals (
    id UUID PRIMARY KEY,
    requester_id UUID NOT NULL REFERENCES users(id),
    provider_id UUID NOT NULL REFERENCES users(id),
    offered_skill_id UUID REFERENCES skill_offers(id),
    requested_skill_id UUID NOT NULL REFERENCES skill_offers(id),
    hours_agreed NUMERIC(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    scheduled_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    proposal_id UUID NOT NULL REFERENCES exchange_proposals(id) ON DELETE CASCADE,
    reviewer_id UUID NOT NULL REFERENCES users(id),
    reviewed_id UUID NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_proposal_reviewer UNIQUE (proposal_id, reviewer_id)
);

CREATE TABLE time_ledger_entries (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    proposal_id UUID REFERENCES exchange_proposals(id),
    amount NUMERIC(10, 2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_skills_category ON skill_offers(category);
CREATE INDEX idx_skills_provider ON skill_offers(provider_id);
CREATE INDEX idx_proposals_requester ON exchange_proposals(requester_id);
CREATE INDEX idx_proposals_provider ON exchange_proposals(provider_id);
CREATE INDEX idx_proposals_status ON exchange_proposals(status);
CREATE INDEX idx_reviews_reviewed ON reviews(reviewed_id);
CREATE INDEX idx_time_ledger_user ON time_ledger_entries(user_id);
