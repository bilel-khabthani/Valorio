# Valorio — Modules Réparation, Don, Recyclage, Authentification & IA

Quatre microservices Spring Boot (modules **4**, **6**, **7** du cahier des charges + module **Authentification & IA**) et un
frontend Angular 20 formant un tableau de bord unique.

| Élément | Techno | Port |
| ------- | ------ | ---- |
| `valorio-backend/service-reparation` | Spring Boot 3.3 / Java 17, JDBC + Flyway | 8081 |
| `valorio-backend/service-don` | Spring Boot 3.3 / Java 17, JDBC + Flyway | 8082 |
| `valorio-backend/service-recyclage` | Spring Boot 3.3 / Java 17, JDBC + Flyway | 8083 |
| `valorio-backend/service-auth` | Spring Boot 3.3 / Java 17, Security, JDBC, Flyway, HMAC-SHA256 | 8084 |
| `valorio-backend/service-marketplace` | Spring Boot 3 / Java 17, JPA, IA & NLP, H2/PostgreSQL | 8085 |
| `valorio-frontend` | Angular 20 (workspace modulaire, 7 libs + pages standalone) | 4200 |
| `valorio-marketplace-frontend` | Angular 17 (Interface ergonomique Lost & Found Marketplace) | 4200 |

Chaque service utilise la base de données PostgreSQL `valorio` ou H2 (schémas `reparation`, `don`, `recyclage`, `auth`, `marketplace`).

## Prérequis

- Java 17, Maven 3.9+
- Node.js 22+ ou 24 et npm
- PostgreSQL local

## 1. Base de données & Environnement

Fichier de variables d'environnement modèle : [`.env.example`](.env.example).

Créer la base (les schémas et tables sont créés automatiquement par Flyway au démarrage des microservices) :

```powershell
$env:PGPASSWORD='root'
psql -U postgres -c "CREATE DATABASE valorio;"
```

Identifiants par défaut : `postgres` / `root` (surchargables via variables d'environnement `DB_USER` / `DB_PASS` / `SPRING_DATASOURCE_*`).

### Comptes pré-configurés (Migration Flyway `auth.users`)

| Rôle | Email | Mot de passe | Statut |
| ---- | ----- | ------------ | ------ |
| **Administrateur** | `admin@valorio.tn` | `Admin123456!` | Actif, vérifié |
| **Utilisateur standard** | `amine.bensalah@example.com` | `User123456!` | Actif, vérifié |

## 2. Backend (4 terminaux à laisser ouverts)

```powershell
mvn -f valorio-backend/service-reparation spring-boot:run   # http://localhost:8081
mvn -f valorio-backend/service-don spring-boot:run          # http://localhost:8082
mvn -f valorio-backend/service-recyclage spring-boot:run    # http://localhost:8083
mvn -f valorio-backend/service-auth spring-boot:run         # http://localhost:8084
mvn -f valorio-backend/service-marketplace spring-boot:run  # http://localhost:8085
```

Vérifications rapides :

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8084/actuator/health
Invoke-RestMethod http://localhost:8085/api/marketplace/stats
```

Documentation interactive & endpoints :

- Réparation : http://localhost:8081/swagger-ui.html
- Don : http://localhost:8082/swagger-ui.html
- Recyclage : http://localhost:8083/swagger-ui.html
- Authentification : http://localhost:8084/swagger-ui.html
- Marketplace API : http://localhost:8085/api/marketplace/items (H2 Console: http://localhost:8085/h2-console)

## 3. Frontend

Lancer l'un des frontends selon le module à tester :

```powershell
# Frontend Global Valorio (Modules Don, Recyclage, Réparation, Auth) :
cd valorio-frontend
npx ng serve          # http://localhost:4200

# Frontend Dédié Marketplace (Module Lost & Found Marketplace IA) :
cd valorio-marketplace-frontend
npm start             # http://localhost:4200
```

Le dev-server redirige (`proxy.conf.json`) :
- `/rep/api` → 8081
- `/don/api` → 8082
- `/rec/api` → 8083
- `/api/auth` → 8084

## Parcours fonctionnels

- **Authentification & Gestion de compte** :
  - Connexion (`/login`) : JWT access token court en mémoire, refresh token rotatif en cookie `HttpOnly` `SameSite=Strict`.
  - Inscription (`/register`) : jauge dynamique de robustesse de mot de passe (8+ car., majuscule, minuscule, chiffre, symbole `@$!%*?#`), vérification par token.
  - Mot de passe oublié & réinitialisation (`/forgot-password`, `/reset-password`) avec envoi réel d'emails asynchrones via Gmail SMTP (`lemonselmi@gmail.com`).
  - Validation email (`/verify-email`).
- **Réparation** (`/reparation`) : créer une demande, rechercher un réparateur, proposer/accepter un devis, planifier une date d'intervention, suivre le workflow `DEMANDE → ACCEPTEE → DIAGNOSTIC → EN_REPARATION → TERMINEE → LIVREE`.
- **Don** (`/don`) : publier un objet (description, conditions, coordonnées), matching des bénéficiaires par distance, workflow de demande `DEMANDE → ACCEPTEE → REMISE_ORGANISEE → CONFIRMEE` (ou `REFUSEE`), annulation logique d'annonce.
- **Recyclage** (`/recyclage`) : analyser la recyclabilité d'un objet, rechercher un centre par matériau/distance, demander une collecte et suivre `DEMANDE → PLANIFIEE → EN_COURS → RECUE` (ou `ANNULEE`).

## Rôles & session

L'application intègre une double gestion de session :
1. **Session Authentifiée Sécurisée** : Connexion via JWT/Spring Security avec affichage du profil connecté, pastille d'avatar, déconnexion avec révocation de session en base.
2. **Sélecteur d'Acteur Métier Rétractable** : Pour préserver la facilité de démonstration des flux multi-acteurs des modules 4, 6 et 7 (Utilisateur, Réparateur, Donneur, Bénéficiaire, Organisme de recyclage, Administrateur).

## Tests automatisés

Exécuter la suite complète de tests (backend et frontend) :

Backend :

```powershell
mvn -f valorio-backend/service-reparation test    # 9 tests
mvn -f valorio-backend/service-don test           # 13 tests
mvn -f valorio-backend/service-recyclage test     # 11 tests
mvn -f valorio-backend/service-auth test          # 20+ tests auth & security
mvn -f valorio-backend/service-marketplace test   # 10 tests unitaires & IA (100% succès)
```

Frontend :

```powershell
cd valorio-frontend
$env:CHROME_BIN="C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
npx ng test --watch=false --browsers=ChromeHeadless
```

Build frontend de production :

```powershell
cd valorio-frontend
npx ng build
```

## Contrats OpenAPI

Les contrats source se trouvent dans `valorio-contracts/` :

- `reparation.openapi.yaml`
- `don.openapi.yaml`
- `recyclage.openapi.yaml`
- `auth.openapi.yaml` (Authentification)

## Dossier Académique IA (Génération de code, Tests & Qualité)

- Méthodologie et preuves d'usage de l'IA pour le module Auth : [`docs/auth-ai-methodology.md`](docs/auth-ai-methodology.md)
- Journal détaillé des prompts, adaptations et anomalies : [`AI_WORKLOG.md`](AI_WORKLOG.md)
- Fiche de validation individuelle Marketplace : [`FICHE_VALIDATION_ORALE_INDIVIDUELLE.md`](FICHE_VALIDATION_ORALE_INDIVIDUELLE.md)
- Rapport complet Projet IA Marketplace & Revalorisation : [`docs/RAPPORT_PROJET_IA_MARKETPLACE.md`](docs/RAPPORT_PROJET_IA_MARKETPLACE.md)

