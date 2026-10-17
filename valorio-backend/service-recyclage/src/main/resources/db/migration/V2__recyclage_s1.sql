CREATE TABLE centre_recyclage (
    id BIGSERIAL PRIMARY KEY,
    organisme_id BIGINT,
    nom VARCHAR(150) NOT NULL,
    ville VARCHAR(80),
    adresse VARCHAR(200),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    materiaux_acceptes VARCHAR(400) NOT NULL,
    horaires VARCHAR(200)
);

CREATE TABLE demande_collecte (
    id BIGSERIAL PRIMARY KEY,
    objet_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    centre_id BIGINT REFERENCES centre_recyclage (id) ON DELETE SET NULL,
    statut VARCHAR(32) NOT NULL,
    date_demande TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_prevue TIMESTAMPTZ,
    date_reception TIMESTAMPTZ
);

CREATE TABLE collecte_historique (
    id BIGSERIAL PRIMARY KEY,
    collecte_id BIGINT NOT NULL REFERENCES demande_collecte (id) ON DELETE CASCADE,
    statut VARCHAR(32) NOT NULL,
    commentaire TEXT,
    date TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO centre_recyclage (organisme_id, nom, ville, adresse, latitude, longitude, materiaux_acceptes, horaires)
VALUES (1, 'Eco-Tunis Centre', 'Tunis', '12 rue de la Nature', 36.8065, 10.1815, 'plastique,aluminium,verre,papier', 'Lun-Ven 08h-17h'),
       (2, 'Sfax Recyclage', 'Sfax', 'Zone industrielle Nord', 34.7406, 10.7600, 'plastique,lithium,verre,cuivre', 'Lun-Sam 09h-18h'),
       (3, 'Sousse Métal', 'Sousse', 'Port de Sousse', 35.8256, 10.6084, 'acier,aluminium,cuivre', 'Lun-Ven 08h-16h'),
       (4, 'Tunis Bois & Carton', 'Tunis', '15 avenue du Bois', 36.8189, 10.1658, 'bois,carton,papier', 'Lun-Ven 07h-15h');
