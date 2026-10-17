CREATE TABLE annonce_don (
    id BIGSERIAL PRIMARY KEY,
    objet_id BIGINT NOT NULL,
    donneur_id BIGINT NOT NULL,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    conditions VARCHAR(400),
    ville VARCHAR(80),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    statut VARCHAR(32) NOT NULL,
    date_publication TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE demande_don (
    id BIGSERIAL PRIMARY KEY,
    annonce_id BIGINT NOT NULL REFERENCES annonce_don (id) ON DELETE CASCADE,
    beneficiaire_id BIGINT NOT NULL,
    message TEXT,
    statut VARCHAR(32) NOT NULL,
    date_demande TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_confirmation TIMESTAMPTZ
);

CREATE TABLE demande_don_historique (
    id BIGSERIAL PRIMARY KEY,
    demande_id BIGINT NOT NULL REFERENCES demande_don (id) ON DELETE CASCADE,
    statut VARCHAR(32) NOT NULL,
    commentaire TEXT,
    date TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO annonce_don (objet_id, donneur_id, titre, description, conditions, ville, latitude, longitude, statut)
VALUES (3, 1, 'Table en bois à donner', 'Table solide, pied à resserrer', 'Retrait sur place', 'Tunis', 36.8065, 10.1815, 'PUBLIEE'),
       (5, 2, 'Manteau d''hiver', 'Manteau taille L, usé mais chaud', 'Aucune', 'Tunis', 36.8065, 10.1815, 'PUBLIEE');
