CREATE SCHEMA IF NOT EXISTS marketplace;

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    evenement VARCHAR(64) NOT NULL,
    entite VARCHAR(128) NOT NULL,
    entite_id BIGINT,
    details TEXT,
    cree_le TIMESTAMPTZ NOT NULL DEFAULT now()
);
