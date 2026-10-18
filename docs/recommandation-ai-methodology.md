# ValorIO — Méthodologie IA, Modélisation et Qualité du Module Recommandation

Ce document formalise et atteste de la prise en compte des exigences pédagogiques relatives à l'**Intelligence Artificielle** dans le cadre du projet **ValorIO** (Plateforme web d'objets trouvés, réparation, dons, recyclage et marketplace), spécifiquement pour le module **Recommandation** et son moteur d'inférence **`ml-engine`**.

Contrairement au module Auth (où l'IA assiste le développement), ce module est le seul à **embarquer un véritable modèle de machine learning entraîné** et exposé en production. Il incarne le critère « intégration des API IA / du modèle ».

---

## 1. Outils, Modèles IA et Rôle

- **Outils de développement** : assistant de développement agentique IA (opencode / LLM spécialisés génie logiciel, ML et data-engineering) pour la génération, la revue et les tests.
- **Modèles réellement entraînés (scikit-learn)** — c'est ici que réside « l'IA » du projet :

| Modèle | Type | Tâche | Performance (jeu de validation) | Version renvoyée |
| ------ | ---- | ----- | ------------------------------- | ---------------- |
| Valorisation | `GradientBoostingClassifier` | Prédire la meilleure voie (Réparer / Vendre / Donner / Recycler) d'un objet trouvé | accuracy **0,8917** | `valorisation-gbm-1.0.0` |
| Acteurs | `GradientBoostingRegressor` | Scorer et classer les acteurs pertinents pour un besoin | R² **0,9365** | `acteurs-gbr-1.0.0` |
| Marketplace | TF-IDF + similarité cosinus + équité de prix | Recommander des articles similaires et signaler les prix aberrants | similarité relative | `marche-tfidf-1.0.1` |

- **Principe d'intégration** : l'IA est un **service d'inférence serveur** (`ml-engine`, FastAPI, port 8090) appelé par le microservice métier `service-recommandation` (port 8087). Aucune clé cloud n'est requise : les modèles sont sérialisés localement (`joblib`), et un **moteur heuristique de repli** prend le relais si le moteur est indisponible. Chaque réponse porte explicitement sa source (`ML` ou `HEURISTIQUE`) et sa `modeleVersion` pour la traçabilité.

---

## 2. Exigence 1 : Génération de Code, Intégration du Modèle et Optimisation

> **Énoncé** : *« Utilisation des outils IA pour l'écriture du code, techniques d'adaptation, optimisation des modèles (LLM) et/ou intégration des API. »*

### 2.1 Architecture générée

1. **Moteur d'inférence `ml-engine`** : application FastAPI (`app/main.py`, `app/schemas.py`, `app/routers/predict.py`, `app/routers/model.py`) exposant `/health`, `/predict/valorisation`, `/predict/acteurs`, `/predict/marche` et `/modele/metadata`.
2. **Entraînement des modèles** : scripts `app/ml/` produisant les trois modèles ci-dessus avec sérialisation `joblib`, entraînés sur un jeu de données synthétique réaliste (coût de réparation, état, catégorie, ancienneté…).
3. **Microservice d'orchestration `service-recommandation`** : conventions Valorio (records, `JdbcClient`, Flyway, RFC 7807), 7 endpoints, persistance de l'historique et du feedback. Il s'alimente dynamiquement des données métier : objets/catégories via `service-objets` (port 8085), utilisateurs/acteurs via `service-auth` (port 8084), annonces via `service-marketplace` (port 8086) (adaptateurs `Integ*`), sans aucune donnée statique (fixtures).
4. **Intégration live entièrement dynamique** : `MarketplaceMarcheClient` lit les annonces réellement disponibles du `service-marketplace` (port 8086). Aucune donnée statique (fixtures JSON / `FixtureLoader` / `Static*Adapter`) n'est utilisée.

### 2.2 Adaptation et optimisation du modèle (exemple concret)

L'optimisation la plus significative a porté sur la **normalisation du texte** pour la recommandation d'articles. La similarité cosinus TF-IDF chutait à cause des diacritiques français (comparaison « vélo » vs « velo »). L'IA a proposé puis affiné la normalisation suivante :

```python
import unicodedata

def _normaliser(texte: str) -> str:
    if not texte:
        return ""
    decompose = unicodedata.normalize("NFKD", texte)
    sans_accents = "".join(c for c in decompose if not unicodedata.combining(c))
    return sans_accents.lower().strip()
```

**Impact mesuré** : sur la requête « VTT », la similarité avec un article « Vélo tout-terrain » est passée de **0,05 à 0,166** ; version du modèle incrémentée `marche-tfidf-1.0.0` → **`1.0.1`**.

### 2.3 Exemple d'intégration Java ↔ Python (corps de requête)

Le Java public et le schéma Pydantic du modèle n'ont pas la même forme ; la couche de traduction a été générée puis revue :

```java
private Map<String, Object> versCorpsPython(ActeursRequest req) {
    Map<String, Object> corps = new LinkedHashMap<>();
    corps.put("typeActeur", req.typeActeur());
    corps.put("position", Map.of("latitude", req.latitude(), "longitude", req.longitude()));
    corps.put("contexte", Map.of("categorieObjet", req.categorieObjet(), "ville", req.ville()));
    corps.put("candidats", req.candidats().stream().map(this::versCandidatPython).toList());
    return corps;
}
```

### 2.4 Revue manuelle et adaptations

| Composant | Suggéré par l'IA | Problème détecté en revue | Adaptation manuelle finale |
| --------- | ---------------- | ------------------------- | -------------------------- |
| `MlClient` | Appel HTTP standard | **HTTP/2 h2c** négocié par défaut → corps perdu → 422 | `.version(HttpClient.Version.HTTP_1_1)` |
| `MlClient.predictActeurs` | Envoyer `ActeursRequest` tel quel | Schéma Pydantic différent → 422 | Ajout de `versCorpsPython()` |
| `marche_model.py` | Similarité cosinus brute | Accents non normalisés → similarité ≈ 0 | `_normaliser()` NFKD + bump de version |
| `MarketplaceMarcheClient` | Jeu d'articles figé | Vocabulaire anglais vs référentiel français | Table de correspondance + repli gracieux |
| `MarcheService` | Échec si marketplace indisponible | Fragilité | Repli sur articles de démonstration |

---

## 3. Exigence 2 : Détection d'Anomalies/Vulnérabilités, Tests Assistés par IA, Refactoring et Évaluation

### 3.1 Anomalies et bugs détectés (et corrigés)

| Anomalie | Symptôme | Cause racine | Correctif | Preuve |
| -------- | -------- | ------------ | --------- | ------ |
| **HTTP/2 h2c** | 422 `{"type":"missing","loc":["body"]}` systématique Java→Python | Le `HttpClient` JDK négocie HTTP/2, `uvicorn`/`h11` rejette l'upgrade et le corps est vidé | `HTTP_1_1` sur les 3 clients | Réponses 200 `source=ML` |
| **Contrat acteurs** | 422 sur `/acteurs` uniquement | Corps Java à plat ≠ schéma Pydantic imbriqué | `versCorpsPython()` | Classement `acteurs-gbr-1.0.0` |
| **Accents** | Similarité d'articles quasi nulle | Absence de normalisation Unicode | `_normaliser()` + `VERSION_MARCHE` 1.0.1 | similarité 0,05 → 0,166 |
| **Collision de ports** | Marketplace et recommandation sur 8086 | Marketplace portée occupe 8086 | Recommandation → **8087**, marketplace → 8086 | Smoke 7/7 |

### 3.2 Tests assistés par IA

| Suite | Outil | Nb tests | Objet |
| ----- | ----- | -------- | ----- |
| `service-recommandation` (JUnit 5 / MockMvc) | Maven | **28** | `RecommandationControllerTest` (6), `ValorisationRegleTest` (8), `RecommandationServiceTest` (14) |
| `ml-engine` (Pytest) | Pytest | **14** | chargement des modèles, schémas Pydantic, bornes de probabilité, stabilité des versions |
| `api-recommandation` (Jasmine/Karma) | Edge Headless | **9** | URLs, verbes, sérialisation des payloads |
| `feature-recommandation` (Jasmine) | Edge Headless | **3** | création, soumission valorisation, feedback |

### 3.3 Refactorings assistés par IA

- **Extraction de la couche de traduction** (`versCorpsPython`) hors du client HTTP, pour isoler le contrat du modèle.
- **Bascule gracieuse** : `MarcheService` n'échoue plus si la marketplace est absente (repli explicite).
- **Traçabilité** : `source` et `modeleVersion` systématiquement propagées à la réponse (refactoring des DTO de réponse).
- **Nettoyage des suites de tests** : correction d'un test de feedback incohérent (qui mockait le service tout en attendant une requête HTTP) → assertion recentrée sur l'état réactif.

### 3.4 Évaluation de performance, fiabilité et **limites**

- **Performance** : inférence `ml-engine` en **< 15 ms** (modèles en mémoire, sérialisés au démarrage) ; les métriques d'entraînement sont exposées via `/modele/metadata` (accuracy 0,8917, R² 0,9365).
- **Fiabilité** : repli heuristique déterministe si le moteur d'IA est indisponible ; la source est explicite côté client (`ML` vs `HEURISTIQUE`), donc **aucune décision n'est masquée**.
- **Limites techniques** : les modèles sont entraînés sur un jeu **synthétique** (volume de données réel faible) ; les prédictions sont des **aides à la décision**, pas des verdicts.
- **Limites éthiques** : risque de **biais** si les données historiques désavantagent certains acteurs ou catégories ; pour cette raison le système reste **explicable** (`ExplicationService`, `/modele/metadata`) et **jamais bloquant** (le repli heuristique garantit un fonctionnement neutre). Aucune donnée personnelle sensible n'est envoyée au moteur d'inférence (seuls des attributs d'objet anonymisés).
- **Adaptation continue** : le feedback utilisateur (`POST /feedback`) est persisté pour de futures itérations d'entraînement.

### 3.5 Validation bout-en-bout (smoke)

Parcours rejoués en conditions réelles sur `integration-recommandation` :

| Endpoint | Résultat observé |
| -------- | ---------------- |
| `POST /api/recommandations/valorisation` (objetId=1) | `source=ML`, route `REPARATION`, probabilité **0,8179** |
| `POST /api/recommandations/valorisation` (corps inline) | `source=ML`, probabilité **0,8834** |
| `POST /api/recommandations/acteurs` | `source=ML`, version `acteurs-gbr-1.0.0` |
| `POST /api/recommandations/marche` | `source=ML`, version `marche-tfidf-1.0.1`, 6 annonces live |
| `POST /api/recommandations/feedback` | enregistré |
| `GET /api/recommandations/modele/metadata` | `actif=true`, métriques et versions |
| `GET /api/recommandations/historique` | historique renvoyé |
| `GET /api/recommandations/{id}/explication` | `source=HEURISTIQUE`, provider `mock` (défaut hors-ligne) |

---

## 4. Synthèse pour l'évaluation

- **Intégration d'un modèle IA** : seule partie du projet à embarquer des modèles entraînés (scikit-learn) exposés via API — démontrable end-to-end.
- **Traçabilité et explicabilité** : source, version, métriques et features exposées ; explication textuelle disponible.
- **Robustesse** : repli heuristique, tolérance panne marketplace, HTTP/1.1 forcé pour la compatibilité.
- **Qualité** : 28 tests JUnit, 14 tests Pytest, 12 specs front pour ce seul module ; **128 tests backend + 76 specs front + 14 Pytest** au total sur la branche d'intégration.
- **Scripts** : `start-all.bat` / `stop-all.bat` pour lancer/arrêter l'ensemble (7 microservices + `ml-engine` + frontend).
