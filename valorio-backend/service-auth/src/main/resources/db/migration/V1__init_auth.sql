CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE IF NOT EXISTS auth.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    email_verifie BOOLEAN NOT NULL DEFAULT FALSE,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_maj TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS auth.refresh_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    date_expiration TIMESTAMPTZ NOT NULL,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS auth.password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    date_expiration TIMESTAMPTZ NOT NULL,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS auth.email_verification_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    date_expiration TIMESTAMPTZ NOT NULL,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON auth.users (email);
CREATE INDEX IF NOT EXISTS idx_refresh_token_hash ON auth.refresh_token (token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON auth.refresh_token (user_id);
CREATE INDEX IF NOT EXISTS idx_pwd_reset_token_hash ON auth.password_reset_token (token_hash);
CREATE INDEX IF NOT EXISTS idx_email_verif_token_hash ON auth.email_verification_token (token_hash);

-- Seed initial admin (Admin123456!) and test user (User123456!)
INSERT INTO auth.users (email, password_hash, nom, prenom, role, actif, email_verifie)
VALUES 
('admin@valorio.tn', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4hM9WqWcmu', 'Admin', 'Système', 'ADMIN', TRUE, TRUE),
('amine.bensalah@example.com', '$2a$12$K.T8x5PZk8pP4XjR0l4.5.3u2Kj8vW9Z1X2c3v4b5n6m7q8w9e0r.', 'Ben Salah', 'Amine', 'USER', TRUE, TRUE)
ON CONFLICT (email) DO NOTHING;
