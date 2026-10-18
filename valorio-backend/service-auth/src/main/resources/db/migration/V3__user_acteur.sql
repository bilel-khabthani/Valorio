-- Acteur métier du compte (5 acteurs du CDC : Utilisateur, Réparateur, Acheteur, Organisme, ADMIN)
ALTER TABLE auth.users
    ADD COLUMN IF NOT EXISTS acteur VARCHAR(30) NOT NULL DEFAULT 'UTILISATEUR';

-- Comptes de démonstration : un acteur par profil
UPDATE auth.users SET acteur = 'ADMIN' WHERE role = 'ADMIN';
UPDATE auth.users SET acteur = 'UTILISATEUR' WHERE email = 'amine.bensalah@example.com';
UPDATE auth.users SET acteur = 'REPARATEUR' WHERE email = 'sara.trabelsi@example.com';
UPDATE auth.users SET acteur = 'ORGANISME' WHERE email = 'karim.gharbi@example.com';

-- Compte de démonstration Acheteur (même mot de passe de démo que les autres seeds)
INSERT INTO auth.users (email, password_hash, nom, prenom, role, acteur, actif, email_verifie, ville, latitude, longitude, score_reputation)
VALUES
('nadia.mansouri@example.com', '$2a$12$w/aCZBl82MUCFUAnddo1g.Ur19dAXqJ3.7tPNjwKEmAeb/bf4G1Qy', 'Mansouri', 'Nadia', 'USER', 'ACHETEUR', TRUE, TRUE, 'Tunis', 36.8065, 10.1815, 4.6)
ON CONFLICT (email) DO NOTHING;
