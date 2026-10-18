ALTER TABLE auth.users
    ADD COLUMN IF NOT EXISTS ville VARCHAR(80),
    ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS score_reputation DOUBLE PRECISION NOT NULL DEFAULT 5.0;

-- Profils des comptes de démonstration (alignés sur les fixtures des autres services)
UPDATE auth.users SET ville = 'Tunis', latitude = 36.8065, longitude = 10.1815
WHERE email = 'admin@valorio.tn';

UPDATE auth.users SET ville = 'Tunis', latitude = 36.8065, longitude = 10.1815, score_reputation = 4.5
WHERE email = 'amine.bensalah@example.com';

-- Utilisateurs de démo (mots de passe de démo, même hash que le seed V1)
INSERT INTO auth.users (email, password_hash, nom, prenom, role, actif, email_verifie, ville, latitude, longitude, score_reputation)
VALUES
('sara.trabelsi@example.com', '$2a$12$w/aCZBl82MUCFUAnddo1g.Ur19dAXqJ3.7tPNjwKEmAeb/bf4G1Qy', 'Trabelsi', 'Sara', 'USER', TRUE, TRUE, 'Sfax', 34.7406, 10.76, 4.0),
('karim.gharbi@example.com', '$2a$12$w/aCZBl82MUCFUAnddo1g.Ur19dAXqJ3.7tPNjwKEmAeb/bf4G1Qy', 'Gharbi', 'Karim', 'USER', TRUE, TRUE, 'Sousse', 35.8256, 10.636, 4.8)
ON CONFLICT (email) DO NOTHING;
