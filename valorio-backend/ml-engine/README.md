# ml-engine — Moteur ML Python de Valorio

Moteur interne (port 8090) alimentant le microservice Java `service-recommandation`
et le contrat `valorio-contracts/recommandation.openapi.yaml`.

## Trois modèles

| Domaine | Algorithme | Artefact |
|---|---|---|
| **Valorisation** — route REPARATION / DON / RECYCLAGE / REVENTE | GradientBoostingClassifier + LogisticRegression (classement des features) | `models/valorisation.joblib` |
| **Acteurs** — probabilité d'acceptation d'un candidat | GradientBoostingRegressor | `models/acteurs.joblib` |
| **Marché** — similarité TF-IDF + équité de prix | TfidfVectorizer (refit par requête) + formule Marketplace | `models/marche.joblib` |

## Démarrage

```bash
cd valorio-backend/ml-engine
python -m training.train          # entraîne et écrit models/ + data/ (~secondes)
uvicorn app.main:app --port 8090  # auto-entraîne au démarrage si models/ absent
```

## Endpoints

- `POST /predict/valorisation` — route recommandée + probabilités + top-5 features explicatives
- `POST /predict/acteurs` — classement des candidats (score, délai, distance, raisons)
- `POST /predict/marche` — articles similaires + scoreDeal + équité de prix
- `GET /model/metadata` — version, métriques du dernier entraînement, top features
- `GET /health` — `{"status": "up"}`

## Tests

```bash
python -m pytest tests -q
```

## Notes

- `training/train_live.py` : re-entraînement depuis PostgreSQL `reco_feedback`
  (requiert `psycopg2` + `RECO_DATABASE_URL`, sinon saut gracieux).
- Catégories/états inconnus → repli `Autre` / `BON`.
- Les artefacts `models/*.joblib` sont gitignorés ; seuls les CSV `data/` sont versionnés.
