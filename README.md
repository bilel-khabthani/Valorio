# Valorio — Modules Réparation, Don, Recyclage, Objets, Marketplace, Recommandation & Authentification/IA

Sept microservices Spring Boot (modules **2**, **4**, **5**, **6**, **7** du cahier des charges + module **Authentification & IA** + module **Recommandation** adossé à un moteur d'IA Python `ml-engine`) et un
frontend Angular 20 formant un tableau de bord unique.

| Élément | Techno | Port |
| ------- | ------ | ---- |
| `valorio-backend/service-reparation` | Spring Boot 3.3 / Java 17, JDBC + Flyway | 8081 |
| `valorio-backend/service-don` | Spring Boot 3.3 / Java 17, JDBC + Flyway | 8082 |
| `valorio-backend/service-recyclage` | Spring Boot 3.3 / Java 17, JDBC + Flyway | 8083 |
| `valorio-backend/service-auth` | Spring Boot 3.3 / Java 17, Security, JDBC, Flyway, HMAC-SHA256 | 8084 |
| `valorio-backend/service-objets` | Spring Boot 3.3 / Java 17, JDBC + Flyway, diagnostic & recommandation | 8085 |
| `valorio-backend/service-marketplace` | Spring Boot 3.3 / Java 17, JDBC + Flyway, moteur IA heuristique | 8086 |
| `valorio-backend/service-recommandation` | Spring Boot 3.3 / Java 17, JDBC + Flyway, orchestration IA + repli heuristique | 8087 |
| `valorio-backend/ml-engine` | Python 3 / FastAPI + scikit-learn (GradientBoosting, TF-IDF) | 8090 |
| `valorio-frontend` | Angular 20 (workspace modulaire, 16 libs + pages standalone) | 4200 |

Chaque service utilise la même base de données PostgreSQL `valorio`, avec un schéma SQL dédié
(`reparation`, `don`, `recyclage`, `objets`, `marketplace`, `recommandation`, `auth`).

> Démarrage tout-en-un : `start-all.bat` (vérifie PostgreSQL puis lance les 7 services, `ml-engine` et le frontend) ; arrêt : `stop-all.bat`.

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

| Acteur | Email | Mot de passe | Statut |
| ------ | ----- | ------------ | ------ |
| **Administrateur** | `admin@valorio.tn` | `Admin123456!` | Actif, vérifié |
| **Utilisateur** | `amine.bensalah@example.com` | `User123456!` | Actif, vérifié |
| **Réparateur** | `sara.trabelsi@example.com` | `User123456!` | Actif, vérifié |
| **Organisme de recyclage** | `karim.gharbi@example.com` | `User123456!` | Actif, vérifié |
| **Acheteur** | `nadia.mansouri@example.com` | `User123456!` | Actif, vérifié |

> Après connexion, l'utilisateur est redirigé vers l'espace de son acteur (`/utilisateur`, `/reparateur`, `/acheteur`, `/organisme`, `/admin`) et la navigation n'affiche que les modules pertinents.

## 2. Backend (7 terminaux + 1 moteur IA à laisser ouverts)

Démarrage conseillé : `service-auth` et `service-objets` d'abord (les services métier les appellent en HTTP), puis `ml-engine` avant `service-recommandation`.

```powershell
mvn -f valorio-backend/service-auth spring-boot:run         # http://localhost:8084
mvn -f valorio-backend/service-objets spring-boot:run       # http://localhost:8085
mvn -f valorio-backend/service-reparation spring-boot:run   # http://localhost:8081
mvn -f valorio-backend/service-don spring-boot:run          # http://localhost:8082
mvn -f valorio-backend/service-recyclage spring-boot:run    # http://localhost:8083
mvn -f valorio-backend/service-marketplace spring-boot:run  # http://localhost:8086
mvn -f valorio-backend/service-recommandation spring-boot:run  # http://localhost:8087

# Moteur d'inférence IA (Python)
cd valorio-backend/ml-engine
python -m uvicorn app.main:app --host 127.0.0.1 --port 8090    # http://localhost:8090
```

Vérifications rapides :

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8084/actuator/health
Invoke-RestMethod http://localhost:8086/actuator/health
Invoke-RestMethod http://localhost:8087/actuator/health
Invoke-RestMethod http://localhost:8090/health
```

Documentation interactive (Swagger UI OpenAPI) :

- Réparation : http://localhost:8081/swagger-ui.html
- Don : http://localhost:8082/swagger-ui.html
- Recyclage : http://localhost:8083/swagger-ui.html
- Authentification : http://localhost:8084/swagger-ui.html
- Objets : http://localhost:8085/swagger-ui.html
- Marketplace : http://localhost:8086/swagger-ui.html
- Recommandation : http://localhost:8087/swagger-ui.html
- Moteur IA (FastAPI) : http://localhost:8090/docs

## 3. Frontend

```powershell
cd valorio-frontend
npx ng serve          # http://localhost:4200
```

Le dev-server redirige (`proxy.conf.json`) :
- `/rep/api` → 8081
- `/don/api` → 8082
- `/rec/api` → 8083
- `/objets/api` → 8085
- `/mkt/api` → 8086
- `/reco/api` → 8087
- `/api/auth`, `/api/users` et `/api/signalements` → 8084

## Parcours fonctionnels

- **Authentification & Gestion de compte** :
  - Connexion (`/login`) : JWT access token court en mémoire, refresh token rotatif en cookie `HttpOnly` `SameSite=Strict`.
  - Inscription (`/register`) : jauge dynamique de robustesse de mot de passe (8+ car., majuscule, minuscule, chiffre, symbole `@$!%*?#`), vérification par token.
  - Mot de passe oublié & réinitialisation (`/forgot-password`, `/reset-password`) avec envoi réel d'emails asynchrones via Gmail SMTP (`lemonselmi@gmail.com`).
  - Validation email (`/verify-email`).
- **Réparation** (`/reparation`) : créer une demande, rechercher un réparateur, proposer/accepter un devis, planifier une date d'intervention, suivre le workflow `DEMANDE → ACCEPTEE → DIAGNOSTIC → EN_REPARATION → TERMINEE → LIVREE`.
- **Don** (`/don`) : publier un objet (description, conditions, coordonnées), matching des bénéficiaires par distance, workflow de demande `DEMANDE → ACCEPTEE → REMISE_ORGANISEE → CONFIRMEE` (ou `REFUSEE`), annulation logique d'annonce.
- **Recyclage** (`/recyclage`) : analyser la recyclabilité d'un objet, rechercher un centre par matériau/distance, demander une collecte et suivre `DEMANDE → PLANIFIEE → EN_COURS → RECUE` (ou `ANNULEE`).
- **Objets** (`/objets`) : registre filtrable des objets trouvés, déclaration, page détail avec diagnostic, recommandation (`REPARER`/`REVENDRE`/`DONNER`/`RECYCLER`), cycle de vie et historique.
- **Marketplace** (`/marketplace`, réservé à l'acteur Acheteur) : catalogue des objets non réclamés revalorisés avec **estimation de prix par IA**, **modération automatique** et **détection d'anomalies** ; dépôt d'annonce assisté par IA, offres d'achat / demandes d'attribution solidaire et tableau de négociations (**Laboratoire IA** : `/marketplace/ai-lab`).
- **Administration** (`/admin`, réservé à l'acteur Administrateur) : tableau de bord avec vue d'ensemble et statistiques ; gestion des **utilisateurs** (activation/désactivation, changement d'acteur, suppression — comptes administrateurs protégés), des **réparateurs**, des **organismes de recyclage**, des **catégories** et des **annonces** (don/marketplace/objets) ; traitement des **signalements** (sous-module 1.3) et **modération** (analyse IA, statut des articles).
- **Recommandation** (`/recommandation`) : moteur d'IA réel (`ml-engine`) exposant (1) la **valorisation** d'un objet trouvé (voie recommandée *Réparer/Vendre/Donner/Recycler* avec probabilité, alternatives et raisons), (2) l'**appariement d'acteurs** (classement des réparateurs/bénéficiaires/centres pertinents), (3) la **recommandation d'articles** de marketplace (articles similaires + équité de prix, alimentée en direct par le `service-marketplace`) ; formulaire de **feedback**, **historique** et panneau de **transparence du modèle** (`/modele/metadata` : métriques, versions, features). Chaque réponse indique sa source (`ML` ou repli `HEURISTIQUE`) et la version du modèle.

## Rôles & session

L'application s'appuie sur une **session authentifiée sécurisée** : connexion via JWT/Spring Security, affichage du profil connecté (pastille d'avatar) et déconnexion avec révocation de session en base. L'interface est **100 % pilotée par l'acteur authentifié** (`acteurGuard`/`accueilGuard`) : chaque acteur (Utilisateur, Réparateur, Acheteur, Organisme de recyclage, Administrateur) est redirigé vers son espace et ne voit que les modules pertinents — l'Administrateur accède en plus à `/admin`.

## Tests automatisés

Exécuter la suite complète de tests (backend et frontend) :

Backend :

```powershell
mvn -f valorio-backend/service-reparation test    # 6 tests
mvn -f valorio-backend/service-don test           # 10 tests
mvn -f valorio-backend/service-recyclage test     # 8 tests
mvn -f valorio-backend/service-auth test          # 35 tests auth, security, administration & signalements
mvn -f valorio-backend/service-objets test        # 22 tests
mvn -f valorio-backend/service-marketplace test   # 19 tests (contrôleurs + moteur IA)
mvn -f valorio-backend/service-recommandation test  # 28 tests (contrôleur, service, règles)
# Total backend : 128 tests
```

Moteur IA (Python) :

```powershell
cd valorio-backend/ml-engine
python -m pytest tests -q                         # 14 tests (modèles, schémas, bornes, versions)
```

Frontend :

```powershell
cd valorio-frontend
$env:CHROME_BIN="C:\Program Files (x86)\Microsoft\EdgeCore\147.0.3912.72\msedge.exe"
npx ng test --watch=false --browsers=ChromeHeadless
# Total frontend : 76 specs (16 projets Angular)
# Suites ciblées de l'espace admin :
npx ng test api-admin --watch=false --browsers=ChromeHeadless      # 19 specs (client HTTP)
npx ng test feature-admin --watch=false --browsers=ChromeHeadless  # 2 specs (tableau de bord + routes)
# Suites ciblées du module recommandation :
npx ng test api-recommandation --watch=false --browsers=ChromeHeadless      # 9 specs (client HTTP)
npx ng test feature-recommandation --watch=false --browsers=ChromeHeadless  # 3 specs (écran)
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
- `objets.openapi.yaml`
- `recommandation.openapi.yaml` (module Recommandation & moteur IA)

## Dossier Académique IA (Génération de code, Tests & Qualité)

- Méthodologie et preuves d'usage de l'IA pour le module Auth : [`docs/auth-ai-methodology.md`](docs/auth-ai-methodology.md)
- Méthodologie et preuves d'usage de l'IA pour le module Recommandation (modèles ML, intégration, limites) : [`docs/recommandation-ai-methodology.md`](docs/recommandation-ai-methodology.md)
- Journal détaillé des prompts, adaptations et anomalies : [`AI_WORKLOG.md`](AI_WORKLOG.md)

