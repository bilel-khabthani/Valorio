CREATE TABLE categorie (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(120) NOT NULL,
    description TEXT
);

CREATE TABLE objet (
    id BIGSERIAL PRIMARY KEY,
    intitule VARCHAR(200) NOT NULL,
    description TEXT,
    categorie_id BIGINT NOT NULL REFERENCES categorie (id),
    etat VARCHAR(32) NOT NULL,
    age_annees INT,
    valeur_estimee DOUBLE PRECISION,
    ville VARCHAR(80),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    materiaux TEXT,
    proprietaire_id BIGINT NOT NULL,
    statut VARCHAR(32) NOT NULL,
    date_declaration TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_maj TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE objet_photo (
    id BIGSERIAL PRIMARY KEY,
    objet_id BIGINT NOT NULL REFERENCES objet (id) ON DELETE CASCADE,
    url TEXT NOT NULL
);

CREATE TABLE diagnostic_objet (
    id BIGSERIAL PRIMARY KEY,
    objet_id BIGINT NOT NULL REFERENCES objet (id) ON DELETE CASCADE,
    panne_declaree TEXT,
    problemes TEXT,
    "analyse" TEXT,
    diagnostic TEXT,
    date TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE historique_objet (
    id BIGSERIAL PRIMARY KEY,
    objet_id BIGINT NOT NULL REFERENCES objet (id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL,
    libelle TEXT,
    acteur_id BIGINT,
    statut_cible VARCHAR(32),
    date TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Jeux de données alignés sur les fixtures des autres services (ids 1 à 5)
INSERT INTO categorie (id, nom, description) VALUES
    (1, 'Electronique', 'Ordinateurs, smartphones et appareils connectes'),
    (2, 'Meubles', 'Mobilier de la maison et du bureau'),
    (3, 'Velo & mobilite', 'Velos, trottinettes et engins de mobilite'),
    (4, 'Vetements', 'Habits, chaussures et accessoires'),
    (5, 'Cuisine', 'Ustensiles et appareils de cuisine');

INSERT INTO objet (id, intitule, description, categorie_id, etat, age_annees, valeur_estimee,
                   ville, latitude, longitude, materiaux, proprietaire_id, statut) VALUES
    (1, 'Ordinateur portable - ecran casse', 'Portable 15 pouces, ecran fendu apres une chute',
     1, 'ENDOMMAGE', 3, 650.0, 'Tunis', 36.8065, 10.1815, 'plastique,aluminium,verre', 1, 'DECLARE'),
    (2, 'Smartphone - batterie usee', 'Smartphone 2 ans, batterie gonflee, charge irreguliere',
     1, 'ENDOMMAGE', 2, 400.0, 'Sfax', 34.7406, 10.76, 'plastique,lithium,verre', 2, 'DECLARE'),
    (3, 'Table en bois - pied desserre', 'Table de repas 4 places, un pied a resserrer',
     2, 'BON', 5, 120.0, 'Tunis', 36.8065, 10.1815, 'bois', 1, 'DECLARE'),
    (4, 'Velo de ville - roue voilee', 'Velo urbain, roue arriere voilee',
     3, 'ENDOMMAGE', 4, 90.0, 'Sousse', 35.8256, 10.636, 'acier,caoutchouc', 3, 'DECLARE'),
    (5, 'Manteau d''hiver - hors d''usage', 'Manteau taille L, doublure dechiree et zip casse',
     4, 'HORS_SERVICE', 8, 15.0, 'Tunis', 36.8065, 10.1815, 'tissu', 2, 'DECLARE');

INSERT INTO historique_objet (objet_id, type, libelle, acteur_id, statut_cible) VALUES
    (1, 'DECLARATION', 'Objet déclaré : Ordinateur portable - ecran casse', 1, 'DECLARE'),
    (2, 'DECLARATION', 'Objet déclaré : Smartphone - batterie usee', 2, 'DECLARE'),
    (3, 'DECLARATION', 'Objet déclaré : Table en bois - pied desserre', 1, 'DECLARE'),
    (4, 'DECLARATION', 'Objet déclaré : Velo de ville - roue voilee', 3, 'DECLARE'),
    (5, 'DECLARATION', 'Objet déclaré : Manteau d''hiver - hors d''usage', 2, 'DECLARE');

INSERT INTO diagnostic_objet (objet_id, panne_declaree, problemes, "analyse", diagnostic) VALUES
    (1, 'Ecran fendu apres une chute',
     'ecran fissure,charniere desseree',
     'Objet ENDOMMAGE, âge 3 ans, valeur estimée 650.0 DT — 2 problème(s) constaté(s) : ecran fissure, charniere desseree. Panne déclarée : Ecran fendu apres une chute.',
     'Réparation complexe : 2 composants concernés, un devis professionnel est recommandé avant toute décision.');

SELECT setval(pg_get_serial_sequence('categorie', 'id'), (SELECT MAX(id) FROM categorie));
SELECT setval(pg_get_serial_sequence('objet', 'id'), (SELECT MAX(id) FROM objet));
