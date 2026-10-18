-- Module 1 - Gestion des utilisateurs, sous-module 1.3 Reputation
-- Signalements de comportements / de contenus (traités par les administrateurs).

CREATE TABLE IF NOT EXISTS auth.signalement (
    id BIGSERIAL PRIMARY KEY,
    signaleur_id BIGINT NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    cible_type VARCHAR(30) NOT NULL,
    cible_id BIGINT,
    motif VARCHAR(60) NOT NULL,
    description TEXT,
    statut VARCHAR(20) NOT NULL DEFAULT 'OUVERT',
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_traitement TIMESTAMPTZ,
    traite_par BIGINT REFERENCES auth.users(id) ON DELETE SET NULL,
    note_traitement TEXT
);

CREATE INDEX IF NOT EXISTS idx_signalement_statut ON auth.signalement (statut);
CREATE INDEX IF NOT EXISTS idx_signalement_cible ON auth.signalement (cible_type, cible_id);
CREATE INDEX IF NOT EXISTS idx_signalement_signaleur ON auth.signalement (signaleur_id);
