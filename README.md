# Valorio — Modules Réparation, Don & Recyclage

Trois microservices Spring Boot (modules **4**, **6**, **7** du cahier des charges) et un
frontend Angular 20 formant un tableau de bord unique.

| Élément | Techno | Port |
| ------- | ------ | ---- |
| `valorio-backend/service-reparation` | Spring Boot 3.3 / Java 17, JDBC + Flyway | 8081 |
| `valorio-backend/service-don` | Spring Boot 3.3 / Java 17, JDBC + Flyway | 8082 |
| `valorio-backend/service-recyclage` | Spring Boot 3.3 / Java 17, JDBC + Flyway | 8083 |
| `valorio-frontend` | Angular 20 (workspace à 7 libs) | 4200 |

Chaque service utilise le même schéma PostgreSQL `valorio`, avec un schéma SQL dédié
(`reparation`, `don`, `recyclage`).

## Prérequis

- Java 17, Maven 3.9+
- Node.js 24.0.2 et npm
- PostgreSQL local

## 1. Base de données

Créer la base (les schémas et les tables sont créés automatiquement par Flyway au démarrage) :

```powershell
$env:PGPASSWORD='bilel'
psql -U postgres -c "CREATE DATABASE valorio;"
```

Identifiants par défaut : `postgres` / `bilel` (surchargables via `${DB_USER}` / `${DB_PASS}`).

## 2. Backend (3 terminaux à laisser ouverts)

```powershell
mvn -f valorio-backend/service-reparation spring-boot:run   # http://localhost:8081
mvn -f valorio-backend/service-don spring-boot:run          # http://localhost:8082
mvn -f valorio-backend/service-recyclage spring-boot:run    # http://localhost:8083
```

Vérifications rapides :

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8081/api/objets/1
```

Documentation interactive (Swagger UI) :

- http://localhost:8081/swagger-ui.html
- http://localhost:8082/swagger-ui.html
- http://localhost:8083/swagger-ui.html

## 3. Frontend

```powershell
cd valorio-frontend
npx ng serve          # http://localhost:4200
```

Le dev-server redirige (`proxy.conf.json`) `/rep/api` → 8081, `/don/api` → 8082, `/rec/api` → 8083.
Les préfixes sont suffixés par `/api` pour ne pas capturer les routes SPA (`/reparation`, `/don`,
`/recyclage`).

## Parcours fonctionnels

- **Réparation** (`/reparation`) : créer une demande, rechercher un réparateur, proposer/accepter
  un devis, planifier une date d'intervention, suivre le workflow `DEMANDE → ACCEPTEE →
  DIAGNOSTIC → EN_REPARATION → TERMINEE → LIVREE`.
- **Don** (`/don`) : publier un objet (description, conditions, coordonnées), matching des
  bénéficiaires par distance, workflow de demande `DEMANDE → ACCEPTEE → REMISE_ORGANISEE →
  CONFIRMEE` (ou `REFUSEE`), annulation logique d'annonce.
- **Recyclage** (`/recyclage`) : analyser la recyclabilité d'un objet, rechercher un centre par
  matériau/distance, demander une collecte et suivre `DEMANDE → PLANIFIEE → EN_COURS → RECUE`
  (ou `ANNULEE`).

Les référentiels externes (`objets`, `utilisateurs`, `catégories`) sont servis par des
adaptateurs **statiques** (`@Profile("!integ")`) : l'application est utilisable sans les autres
modules.

## Rôles & session simulée

Le module 1 (authentification) n'étant pas implémenté, un **sélecteur de session** est présent
dans la barre du haut : on choisit un **rôle** puis une **identité** ; le choix est persisté dans
`localStorage` (`valorio.session`). Chaque page se découpe alors en **sections de rôle** étiquetées.

| Rôle | Réparation | Don | Recyclage |
| ---- | ---------- | --- | --------- |
| Utilisateur | créer une demande, accepter/refuser le devis, planifier l'intervention | — | analyser, rechercher un centre, demander/suivre une collecte |
| Réparateur | créer le devis, faire évoluer le statut | — | — |
| Donneur | — | publier, gérer ses annonces et les demandes reçues | — |
| Bénéficiaire | — | parcourir le catalogue, demander un objet, suivre sa demande | — |
| Organisme de recyclage | — | — | traiter les collectes de son centre |
| Administrateur | voit et agit sur tout, avec les actions donneur + bénéficiaire / utilisateur + organisme |

Hors rôle, la section affiche une invite invitant à changer de rôle. Les acteurs non-administrateurs
voient leur identité auto-remplie en lecture seule.

## Tests

Backend (par service) :

```powershell
mvn -f valorio-backend/service-reparation package
mvn -f valorio-backend/service-don package
mvn -f valorio-backend/service-recyclage package
```

Frontend :

```powershell
cd valorio-frontend
$env:CHROME_BIN="C:\Program Files\Google\Chrome\Application\chrome.exe"
npx ng test shared --watch=false --browsers=ChromeHeadless
# idem pour api-reparation, api-don, api-recyclage, feature-reparation, feature-don, feature-recyclage, valorio-frontend
```

## Contrats OpenAPI

Les contrats source se trouvent dans `valorio-contracts/` :

- `reparation.openapi.yaml`
- `don.openapi.yaml`
- `recyclage.openapi.yaml`

## Journal d'usage de l'IA

Voir [`AI_WORKLOG.md`](AI_WORKLOG.md) (phases S0, S1, S2, S3).
