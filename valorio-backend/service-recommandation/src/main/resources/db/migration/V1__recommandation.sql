CREATE SCHEMA IF NOT EXISTS recommandation;

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    evenement VARCHAR(64) NOT NULL,
    entite VARCHAR(128) NOT NULL,
    entite_id BIGINT,
    details TEXT,
    cree_le TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE reco_historique (
    id BIGSERIAL PRIMARY KEY,
    objet_id BIGINT,
    type_reco VARCHAR(32) NOT NULL,
    charge_utilite TEXT,
    resultat TEXT,
    source VARCHAR(16) NOT NULL,
    modele_version VARCHAR(64),
    cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE reco_feedback (
    id BIGSERIAL PRIMARY KEY,
    recommandation_id BIGINT NOT NULL,
    utile BOOLEAN NOT NULL,
    commentaire VARCHAR(500),
    enregistre_le TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE modele_metadata (
    id BIGSERIAL PRIMARY KEY,
    nom_modele VARCHAR(64) UNIQUE NOT NULL,
    version VARCHAR(64),
    actif BOOLEAN DEFAULT true,
    metriques JSONB,
    top_features TEXT,
    maj_le TIMESTAMPTZ DEFAULT now()
);

INSERT INTO modele_metadata (nom_modele, version, actif, metriques, top_features)
VALUES ('valorisation', 'valorisation-gbm-1.0.0', TRUE, '{}'::jsonb,
        'coutRatio,etat,ageAnnees,valeurEstimee,tauxRecyclabilite'),
       ('acteurs', 'acteurs-gbr-1.0.0', TRUE, '{}'::jsonb,
        'noteMoyenne,matchCategorie,distanceKm,disponible'),
       ('marche', 'marche-tfidf-1.0.0', TRUE, '{}'::jsonb,
        'similarite,prix,etat,ageAnnees');
