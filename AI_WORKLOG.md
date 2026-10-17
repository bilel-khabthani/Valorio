# AI_WORKLOG — Valorio (modules Don / Recyclage / Réparation)

Journal de preuves de l'usage de l'IA, tenu **à chaque tâche**, source unique pour la présentation.
Règle : toute tâche de développement produit (1) le code et (2) son entrée dans ce journal.

## Colonnes

| Date | Phase | Tâche | Outil | Technique IA | Prompt (résumé) | Résultat IA | Adaptation humaine | Tests générés | Anomalie détectée → correctif | Métrique |
| ---- | ----- | ----- | ----- | ------------ | --------------- | ----------- | ------------------ | ------------- | ----------------------------- | -------- |

## Instructions internes (workflow)

1. Avant de coder : fournir à l'IA le contexte (extrait du CDC, contrat OpenAPI, conventions du projet).
2. Après génération : revue humaine systématique, adaptation au style du dépôt, puis exécution des tests.
3. En cas d'échec : coller l'erreur exacte à l'IA, demander un diagnostic + correctif, re-tester.
4. Consigner : prompt, réponse, ce qui a été gardé/refusé, tests générés, anomalies, temps gagné.

## Comment lancer l'environnement (sans Docker)

PostgreSQL local (base `valorio`, utilisateur `postgres`) :

```powershell
$env:PGPASSWORD='bilel'
psql -U postgres -c "CREATE DATABASE valorio;"
```

Backend (3 terminaux, laisser tourner) :

```powershell
mvn -f valorio-backend/service-reparation spring-boot:run   # http://localhost:8081
mvn -f valorio-backend/service-don spring-boot:run          # http://localhost:8082
mvn -f valorio-backend/service-recyclage spring-boot:run    # http://localhost:8083
```

Frontend :

```powershell
cd valorio-frontend
npx ng serve          # http://localhost:4200, proxy /rep /don /rec vers 8081/8082/8083
```

Vérifications rapides :

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8081/api/objets/1
Invoke-RestMethod http://localhost:4200/rep/api/objets/1   # via proxy Angular
```

---

## Phase S0 — Fondations (contrats, squelettes, stubs statiques, workspace)

| Date | Phase | Tâche | Outil | Technique IA | Prompt (résumé) | Résultat IA | Adaptation humaine | Tests générés | Anomalie détectée → correctif | Métrique |
| ---- | ----- | ----- | ----- | ------------ | --------------- | ----------- | ------------------ | ------------- | ----------------------------- | -------- |
| 2026-10-16 | S0 | Contrats | Contrat-first | opencode | Prompt structuré (rôle + CDC + stack + contraintes), sortie contrainte en OpenAPI 3.0.3 | « À partir du module 6/7/4 du CDC, produis le contrat OpenAPI des endpoints Don/Recyclage/Réparation, entités externes incluses en lecture seule » | 3 fichiers `valorio-contracts/*.openapi.yaml` (tags = sous-modules du CDC, machines à états en enums) | Renommage cohérent (`beneficiaireId`), versionnage des fichiers, serveurs pointant sur les ports locaux | — (contrat = source de vérité pour les tests de contrat S4) | Champ typographié `beneciaireId` dans le DTO → corrigé par recherche/remplacement | 3 contrats, ~60 endpoints/schemas |
| 2026-10-16 | S0 | Backend | Squelette service | opencode | Chaînage spec → code (le contrat sert de contexte), génération de structure par module | « Implémente un service Spring Boot 3.3 / Java 17 conforme au contrat réparation : ports + adaptateurs statiques chargés depuis fixtures JSON, Flyway, profil `!integ` » | Arborescence `com.valorio.reparation` : 3 ports, 3 adaptateurs `@Profile("!integ")`, 3 contrôleurs, `ApiExceptionHandler` (ProblemDetail), `FixtureLoader`, `V1__init.sql` | Suppression de Lombok (explicite, zéro config IDE), records Java plutôt que POJO, pas de gateway (choix « sans Docker ») | `StaticObjetAdapterTest` (3 cas : findById, inconnu, filtre catégorie) généré par l'IA | Adapter unique implémentant 3 ports → collision `findById` (types de retour différents) détectée par compilation → un adaptateur par port | 14 fichiers, tests verts |
| 2026-10-16 | S0 | Backend | Duplication Don/Recyclage | opencode | Génération par transformation contrôlée (script) plutôt que re-génération IA | « Duplique le service en remplaçant package, port, schéma et nom d'application » | Script PowerShell de copie + réécriture (packages, ports 8082/8083, schémas `don`/`recyclage`) | Vérification fichier par fichier des occurrences restantes | Tests unitaires identiques exécutés sur les 3 modules | « Don » ne compilait pas : classe `DonApplication` dans un fichier `ReparationApplication.java` (renommage de contenu sans renommer le fichier) → renommage effectué, build vert | 2 services générés en < 1 min |
| 2026-10-16 | S0 | Infra locale | Base PostgreSQL | opencode | — | « Crée la base et vérifie les migrations Flyway » | Schémas `reparation`, `don`, `recyclage` créés automatiquement par Flyway (chacun : `audit_log` + `flyway_schema_history`) | Choix de 1 base / 3 schémas, credentials via `${DB_USER}` / `${DB_PASS}` | Vérification SQL `information_schema.tables` (6 lignes) | Connexion `psql` bloquée faute de mot de passe → usage de `PGPASSWORD` | 3 schémas, 3 services UP |
| 2026-10-16 | S0 | Frontend | Workspace Angular | opencode | Génération scaffolder + adaptation | « Crée un workspace Angular 20 avec 7 libs (shared, api-*, feature-*) et un shell à routes lazy » | `valorio-frontend` + 7 libs sous `projects/` | Paths tsconfig repointés vers `src/public-api.ts` (au lieu de `dist/`) pour itérer sans builder les libs ; scopes `@valorio/*` | Specs de composant/services générées (TestBed + provideHttpClientTesting) | Angular CLI 22 rejeté par Node 24.0.2 (engine) → repli sur Angular CLI 20 compatible | 7 libs, build prod OK (262 kB initial, 3 lazy chunks) |
| 2026-10-16 | S0 | Frontend | Proxy + wiring | opencode | — | « Configure le proxy de dev vers les 3 services et vérifie via un appel réel » | `proxy.conf.json` (`/rep`→8081, `/don`→8082, `/rec`→8083 avec pathRewrite) + `angular.json` serve options | Libellés FR, styles shell | Vérification bout-en-bout (HTTP via 4200) | Proxy 500 : cause = services pas encore démarrés (pas un bug de config) → attente du `/actuator/health` avant test | `/rep/api/objets/1` → « Ordinateur portable - ecran casse » |
| 2026-10-16 | S0 | QA | Détection d'anomalies | opencode | Boucle test → erreur collée à l'IA → diagnostic → correctif → re-test | « Voici l'erreur de build/démarrage, diagnostique et corrige » | 4 anomalies détectées et corrigées (voir colonne précédente) | Revue humaine de chaque correctif | — | Voir anomalies ci-dessus | 4 anomalies / 4 corrigées, 0 régression (builds verts) |

### Bilan S0 (à enrichir en S1+)

- Artefacts livrés : 3 contrats OpenAPI, 3 services Spring Boot, 1 workspace Angular (7 libs + shell + proxy), 3 schémas PostgreSQL, fixtures statiques, **9 tests backend (3 par service, verts) + 10 specs front (verts)**.
- Stubs statiques : `Objet`, `Utilisateur`, `Categorie` servis par les adaptateurs `@Profile("!integ")` → dev/tests 100 % locaux sans dépendre des autres modules. Bascule dynamique prévue en S5 (`@Profile("integ")`, `VALORIO_ENV.useStaticApis = false`).
- Techniques IA effectivement utilisées : prompt structuré (rôle + CDC + stack + contraintes), chaînage contrat → code, génération de tests par lecture du code, boucle d'auto-correction sur erreurs réelles, transformation contrôlée par script pour la duplication.

---

## Phase S1 — Cœur métier (workflows, règles, persistance) + frontend fonctionnel

| Date | Phase | Tâche | Outil | Technique IA | Prompt (résumé) | Résultat IA | Adaptation humaine | Tests générés | Anomalie détectée → correctif | Métrique |
| ---- | ----- | ----- | ----- | ------------ | --------------- | ----------- | ------------------ | ------------- | ----------------------------- | -------- |
| 2026-10-16 | S1 | Réparation | Domaine + persistance | opencode | Décision d'architecture revue par l'IA (comparaison JPA vs JDBC) puis génération sur patron | « Abandonne JPA : passe les 3 services en spring-boot-starter-jdbc + JdbcClient, écris la migration V2 et le domaine Réparation (workflow StatutDemande, devis, réparateurs), service transactionnel + contrôleurs + handler » | `V2__reparation_s1.sql` (4 tables + 3 réparateurs seedés), `domain/` (StatutDemande avec `peutTransitionnerVers`, Devis, Reparateur…), `ReparationService` (`@Transactional`, gardes métier), 3 contrôleurs, `AuditLog` | Records plutôt que POJO, SQL paramétré, garde « devis uniquement sur demande DEMANDE », garde « ACCEPTEE ⇒ réparateur assigné » | `StatutDemandeTest` (3 cas : suite nominale, saut interdit, fin de workflow) | `TransitionInterditeException extends RuntimeException` → non mappée par `ApiExceptionHandler` (409 attendu) → passage à `extends IllegalStateException` (mappée 409) dans les 3 services | 1 migration, 8 fichiers, 3 tests ajoutés |
| 2026-10-16 | S1 | Don | Domaine + matching | opencode | Réplication du patron S1 sur le module 6 + règle de matching géographique | « Implémente le module Don : statuts annonce/demande, matching par distance (Haversine) et score, demandes de don avec workflow » | `V2__don_s1.sql` (3 tables + 2 annonces), `domain/` (StatutAnnonceDon, StatutDemandeDon, AnnonceDon, DemandeDon…), `util/Geo` (Haversine), `DonService` (matching distance+score, workflow demande ↔ statut annonce), 2 contrôleurs | Seuil de validation du DTO, arrondi 2 décimales, filtre catégorie via `ObjetPort` | `GeoTest` (Tunis↔Sfax ≈ 225-245 km, même point = 0), `StatutDemandeDonTest` (3 cas dont refus avant confirmation) | Route `/matching` déclarée avant `/{id}` pour éviter la capture du segment (ordre des mappings) | 6 fichiers, 5 tests ajoutés |
| 2026-10-16 | S1 | Recyclage | Règle de recyclabilité + centres | opencode | Génération d'une règle pure testable + recherche par distance | « Implémente le module Recyclage : règle de recyclabilité (matériaux valorisables), centres acceptant des matériaux + distance, workflow de collecte horodatée » | `V2__recyclage_s1.sql` (3 tables + 3 centres), `RegleRecyclabilite` (OUI/PARTIEL/NON + motifs), `RecyclageService` (analyse + centres appropriés, filtre distance, transitions horodatées), 3 contrôleurs | Normalisation casse/doublons des matériaux, mots-clés de motifs FR | `RegleRecyclabiliteTest` (5 cas), `StatutCollecteTest` (3 cas) | `stream().collect(LinkedHashSet::new, Set::add, Set::addAll)` → `List<Object>` non convertible (inférence de types) → réécriture en boucle `LinkedHashSet` explicite | 7 fichiers, 8 tests ajoutés |
| 2026-10-16 | S1 | Frontend | Modèles + clients API | opencode | Contrat-first : dérivation des interfaces TS depuis les records Java contrôlés | « Ajoute dans `@valorio/shared` les modèles des 3 modules + helpers de workflow, puis étends les 3 services API (demandes, devis, matching, centres, collectes) » | `models.ts` (types + `prochainStatut*` + `libelleStatut` + `formaterDate`), 3 services HTTP complets typés | Gestion d'erreur centralisée `messageErreur` (ProblemDetail → texte), helpers partagés | `models.spec.ts` (4 cas sur les helpers de workflow) | — | 4 fichiers, 4 tests |
| 2026-10-16 | S1 | Frontend | 3 features + design system | opencode | Génération de composants standalone (signals, control flow `@if/@for`) depuis les modèles | « Construis les écrans Réparation (création + devis + workflow), Don (publication + matching + demandes), Recyclage (analyse + centres + collectes), avec routes lazy et styles partagés » | 3 features (7 composants), routes avec détail (`demandes/:id`, `annonces/:id`, `collectes/:id`), design system `styles.css`, barre de navigation | Styles et libellés FR revus, gestion des cas vides/erreurs | Specs de composants mises à jour (HttpClientTesting + Router) | Balise fermante parasite `</span>` dans un `<option>` (NG5002) ; getter `id` manquant sur le composant détail (TS2339) → corrigés | 9 fichiers, build prod OK |
| 2026-10-16 | S1 | QA | Vérification bout-en-bout | opencode | Exécution réelle (build, tests, appels HTTP) comme preuve | « Démarre les 3 jars, joue les parcours et les cas d'erreur via HTTP, corrige ce qui casse » | Parcours validés en direct : demande→devis→acceptation (Réparation), annonce→matching→demande→acceptation (Don), analyse→centre→collecte (Recyclage) ; erreurs métier renvoyées en 409 | Tests unitaires ciblés ajoutés après échec (boucle de debug) | 14 specs front vertes ; smoke tests HTTP (201/409, matching, distance) | Port 8081 saturé par des instances `java` orphelines → arrêt des processus et relance propre | Backend : 25 tests verts (9 S0 + 16 S1) ; Front : 14 specs vertes ; 3 proxies OK via 4200 |

### Bilan S1

- Backend : les 3 modules exposent désormais le cœur métier — workflows d'états validés côté service (`TransitionInterditeException` → HTTP 409), devis + assignation réparateur (Réparation), matching par distance Haversine + score (Don), règle de recyclabilité + recherche de centres par matériau/distance + collecte horodatée (Recyclage). Chaque action sensible écrit dans `audit_log`.
- Persistance : passage à `spring-boot-starter-jdbc` + `JdbcClient` (records, SQL paramétré, migrations Flyway `V2__*`), moins de boilerplate que JPA et requêtes explicites.
- Frontend : workspace Angular 20 fonctionnel — écrans complets pour les 3 modules (création, listes filtrables, détail avec workflow et devis/demandes/collectes), design system partagé, clients API typés et gestion d'erreurs uniforme (messages ProblemDetail).
- Qualité : backend **25 tests** verts (contexte Spring + règles via `mvn package`), frontend **14 specs** vertes (`ng test`), et parcours bout-en-bout confirmés par appels HTTP réels à travers le proxy Angular (4200 → 8081/8082/8083).
- Anomalies S1 corrigées : mapping 409 manquant (héritage d'exception), inférence de generics sur `Stream.collect`, balise HTML fermante parasite, getter manquant exposé au template, et saturation du port 8081 par des processus résiduels.

---

## Phase S2 — Complétude CDC (date d'intervention, annulation de don, libellés, Swagger)

| Date | Phase | Tâche | Outil | Technique IA | Prompt (résumé) | Résultat IA | Adaptation humaine | Tests générés | Anomalie détectée → correctif | Métrique |
| ---- | ----- | ----- | ----- | ------------ | --------------- | ----------- | ------------------ | ------------- | ----------------------------- | -------- |
| 2026-10-17 | S2 | Réparation | 4.2 Date d'intervention | opencode | Génération contrat → code → test sur un ajout de champ | « Ajoute la planification d'intervention : migration V3 (`date_intervention`), champ dans le record domaine, `majDateIntervention` (repo), `planifierIntervention` (service, garde statut ≥ ACCEPTEE), endpoint `PUT /api/demandes/{id}/intervention`, schéma OpenAPI, test de domaine » | Migration `V3__reparation_intervention.sql`, `StatutDemande.autorisePlanificationIntervention()`, `ReparationService.planifierIntervention`, `DemandeController.PlanifierInterventionRequest`, maj `SELECT_DEMANDE`/mapper | Garde métier dans l'enum (testable sans DB), historique écrit avec la date, validation `dateIntervention` obligatoire | `StatutDemandeTest#planifie...` (DEMANDE/LIVREE refusés), `DemandeControllerTest` (`@WebMvcTest` : 200 + 400) | Mapping JDBC par position : oubli de `dateIntervention` entre `statut` et `dateCreation` aurait décalé tous les champs → ajout aligné sur le record | 1 migration, 6 fichiers, 2 tests |
| 2026-10-17 | S2 | Don | Annulation logique (6.x) | opencode | Ajout d'endpoint + garde de transition depuis l'enum existant | « Ajoute l'annulation d'annonce : `DonService.annuler` (contrôle donneur + transition vers ANNULEE), endpoint `PUT /api/dons/{id}/annuler`, contrat, test du statut » | `DonService.annuler(long, Long)`, `DonController` endpoint, path OpenAPI, test `StatutAnnonceDonTest` (3 cas) | Réutilisation de `peutTransitionnerVers` (pas de logique dupliquée), `donneurId` optionnel | `StatutAnnonceDonTest` (publiée→annulée, réservée→annulée, REMISE/ANNULEE refusés) + `DonControllerTest` (`@WebMvcTest`) | — | 3 fichiers, 4 tests |
| 2026-10-17 | S2 | Transverse | Libellés au lieu d'IDs | opencode | Refactor guidé par contrainte produit (UX) | « Remplace la saisie/affichage d'identifiants par des sélecteurs et libellés : ajoute `GET /api/utilisateurs` (liste) sur les 3 backends, les helpers `labelObjet`/`labelUtilisateur`/`labelCentre`/`labelReparateur`/`indexParId`, puis convertis les formulaires et tableaux des 3 modules » | `UtilisateurPort.findAll` + `StaticUtilisateurAdapter.findAll` + `UtilisateurController.GET` (×3), helpers `@valorio/shared`, sélecteurs objets/utilisateurs/centres/réparateurs et colonnes libellées dans les 6 composants | Mise en cache des référentiels via `computed` (Map id→entité), repli `#id` si l'entité est absente | Specs de composants (création) toujours vertes ; specs API étendues | `labelCentre` utilisé dans un template sans être exposé par le composant (TS2339) → ajout d'un champ `readonly labelCentre` | 3 endpoints, 6 composants, 6 helpers |
| 2026-10-17 | S2 | Don/Recyclage | Formulaire Don enrichi + B3 filtre réparateur | opencode | — | « Complète la publication de don (description, conditions, coordonnées) et ajoute un filtre par réparateur sur la liste des demandes » | Champs `description/conditions/latitude/longitude` (Don), `filtreReparateurId` câblé au paramètre `reparateurId` (Réparation) | — | Build front vert | — | 2 composants |
| 2026-10-17 | S2 | Contrats/QA | Swagger + tests de contrat | opencode | Complétude CDC (`/swagger-ui`) + tests HTTP isolés | « Ajoute springdoc-openapi (3 poms) pour exposer `/swagger-ui.html`, et des tests de contrôleur `@WebMvcTest` (service mocké, sans base) pour valider les nouveaux endpoints, plus des specs API front vérifiant URL/méthode » | `springdoc-openapi-starter-webmvc-ui` 2.6.0, `DemandeControllerTest` + `DonControllerTest`, specs `planifierIntervention`/`annulerAnnonce` (`HttpTestingController`) | Tests de contrôleur volontairement sans `@SpringBootTest` (pas de dépendance PostgreSQL) | 3 tests de contrôleur + 2 specs API ajoutés | — | 3 poms, 2 tests back, 2 specs front |

### Bilan S2

- Complétude fonctionnelle CDC : la date d'intervention (Réparation 4.2) et l'annulation logique d'annonce (Don) sont désormais exposées de bout en bout (migration → service → endpoint → contrat → UI), et la règle « statut ≥ ACCEPTEE » est portée par le domaine (testable).
- UX : plus aucun identifiant saisi ou affiché — les 3 modules peuplent des sélecteurs (objet, utilisateur, centre, réparateur) et résolvent les libellés via `@valorio/shared` ; les appels référentiels passent par `GET /api/utilisateurs` ajouté sur les 3 backends.
- Qualité : tests de contrôleur `@WebMvcTest` (sans base) sur les nouveaux endpoints, specs front vérifiant les URL/méthodes, et Swagger UI (`/swagger-ui.html`) disponible sur les 3 services.
- État global vérifié : `mvn package` (3 services) vert, `ng build` vert, `ng test` (7 projets) vert, contrats OpenAPI réalignés sur le code.

---

## Phase S3 — Rôles & session simulée + correction des 404 de routes SPA

| Date | Phase | Tâche | Outil | Technique IA | Prompt (résumé) | Résultat IA | Adaptation humaine | Tests générés | Anomalie détectée → correctif | Métrique |
| ---- | ----- | ----- | ----- | ------------ | --------------- | ----------- | ------------------ | ------------- | ----------------------------- | -------- |
| 2026-10-17 | S3 | Frontend | Diagnostic des 404 | opencode | Diagnostic par lecture du code + preuves HTTP réelles | « L'app renvoie 404 sur `/reparation`, `/don`, `/recyclage` et sur les appels API ; identifie la cause racine » | Cause identifiée : `proxy.conf.json` déclarait `/rep`, `/don`, `/rec` traités comme **préfixes** par `http-proxy-middleware` → `/reparation` était proxifié vers 8081 sous `/aration` (404), au lieu d'être servi par le fallback SPA | Vérification que le fallback SPA natif du dev-server Angular Vite fonctionne (`appType: 'custom'`) : ce n'était pas la cause | Smoke tests HTTP (`/reparation`, `/don`, `/recyclage` → 200) | Hypothèse initiale (fallback SPA manquant) invalidée par lecture de `html-fallback-middleware.js` → vraie cause = clés de proxy trop courtes | 1 fichier, 3 routes réparées |
| 2026-10-17 | S3 | Frontend | Correctif proxy | opencode | Correctif minimal guidé par la cause racine | « Fais pointer les clés de proxy sur `/rep/api`, `/don/api`, `/rec/api` en conservant les `pathRewrite` » | `proxy.conf.json` : clés `/rep/api`,`/don/api`,`/rec/api`, `pathRewrite {"^/rep": ""}`… | Redémarrage du dev-server pour recharger le proxy | Vérif HTTP : SPA → 200, `/{rep,don,rec}/api/utilisateurs` → 200 | — | 1 fichier, 6 routes vérifiées |
| 2026-10-17 | S3 | Transverse | Titres sans « Module » | opencode | — | « Retire le mot Module des titres des 3 écrans » | `feature-reparation`/`feature-don`/`feature-recyclage` : h1 → `Réparation`/`Don`/`Recyclage` | — | Build front vert | — | 3 composants |
| 2026-10-17 | S3 | Frontend | Rôles & session | opencode | Génération d'une brique transverse (signals + persistance) puis câblage guidé par le CDC (5 acteurs) | « Module 1 non implémenté : ajoute un `SessionService` (rôles, identité, localStorage) et un sélecteur rôle+identité dans la barre du haut, puis découpe chaque page en sections par rôle » | `shared/session.ts` (`RoleActeur`, `SessionService`, helpers d'identité), switcher dans `app.ts/html/css`, sections `role-section`/`section-title` | Rôles retenus : Utilisateur, Réparateur, Donneur, Bénéficiaire, Organisme, Admin ; vue Admin incluse ; acteur auto-rempli hors admin | `session.spec.ts` (3 cas : défaut, connexion donneur, admin) | `computed` lisant une propriété non-signal (switcher non réactif) et conflits méthode/`computed` de même nom (`peutCreerDevis`) → refactor en méthode/`computed` uniques | 1 service, 1 switcher, 6 composants, 3 specs |
| 2026-10-17 | S3 | Frontend | Sections de rôle | opencode | Contrats → templates (gating par rôle) | « Répartitionne les actions par rôle dans les 6 composants et masque ce qui ne relève pas du rôle courant » | Réparation (demandeur vs réparateur), Don (donneur vs bénéficiaire), Recyclage (utilisateur vs organisme), Admin = union ; filtrage serveur (`utilisateurId`/`reparateurId`/`centreId`) selon la session | Gating `peutRepondreDevis`/`peutCreerDevis`/`peutPlanifier`/`peutAvancer` porté par la session | Build vert + reprise des specs existantes | Accès à `session` privé depuis les templates (TS2341) ; `horsRole`/`chargerReparateurs` manquants (TS2339) ; mismatch `<span class="field">…</label>` → corrigés | 6 composants, build vert |
| 2026-10-17 | S3 | QA | Vérification | opencode | Exécution réelle (build + tests) comme preuve | « Rejoue build et tests après le refactor rôles » | `ng build` vert, `ng test` : 16 specs + 3 nouvelles = **19 specs vertes** (7 projets) | — | — | — | 19 specs front vertes |

### Bilan S3

- Cause racine des 404 identifiée et corrigée : le proxy utilisait `/rep`,`/don`,`/rec` comme préfixes (donc `/reparation` était capturé) ; les clés sont désormais `/rep/api`,`/don/api`,`/rec/api`.
- Les 3 pages sont découpées en **sections de rôle** étiquetées et un **sélecteur de session** (rôle + identité, persistant) simule la connexion en attendant le module 1, avec une vue **Administrateur** unifiée.
- Vérification : `ng build` vert et `ng test` (7 projets) vert après le refactor des rôles.


