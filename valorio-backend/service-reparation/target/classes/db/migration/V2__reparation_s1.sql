CREATE TABLE reparateur (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    ville VARCHAR(80),
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    note_moyenne NUMERIC(3, 2) NOT NULL DEFAULT 0,
    nb_avis INTEGER NOT NULL DEFAULT 0,
    competences VARCHAR(400),
    categories VARCHAR(400)
);

CREATE TABLE demande_reparation (
    id BIGSERIAL PRIMARY KEY,
    objet_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    reparateur_id BIGINT REFERENCES reparateur (id),
    description_panne TEXT NOT NULL,
    statut VARCHAR(32) NOT NULL,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_maj TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE demande_statut_historique (
    id BIGSERIAL PRIMARY KEY,
    demande_id BIGINT NOT NULL REFERENCES demande_reparation (id) ON DELETE CASCADE,
    statut VARCHAR(32) NOT NULL,
    commentaire TEXT,
    date TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE devis (
    id BIGSERIAL PRIMARY KEY,
    demande_id BIGINT NOT NULL REFERENCES demande_reparation (id) ON DELETE CASCADE,
    montant NUMERIC(10, 2) NOT NULL,
    delai_jours INTEGER,
    commentaire TEXT,
    statut VARCHAR(32) NOT NULL,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO reparateur (utilisateur_id, ville, disponible, note_moyenne, nb_avis, competences, categories)
VALUES (1, 'Tunis', TRUE, 4.60, 18, 'electronique,ecran,carte mere', 'Electronique'),
       (2, 'Sfax', TRUE, 4.20, 9, 'menuiserie,bois,meubles', 'Meubles'),
       (3, 'Sousse', TRUE, 3.90, 5, 'velo,freins,transmission', 'Velo & mobilite');
