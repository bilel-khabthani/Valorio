"""Modèle de classement des acteurs (GradientBoostingRegressor — probabilité d'acceptation)."""
from __future__ import annotations

import numpy as np
from sklearn.ensemble import GradientBoostingRegressor

from app.ml.features import (
    ACTEURS_FEATURE_NAMES,
    build_acteurs_features,
    haversine_km,
    match_categorie,
)

VERSION_ACTEURS = "acteurs-gbr-1.0.0"


def train_bundle(X: np.ndarray, y: np.ndarray, random_state: int = 42) -> tuple[dict, dict]:
    from sklearn.metrics import mean_squared_error, r2_score
    from sklearn.model_selection import train_test_split

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=random_state
    )
    model = GradientBoostingRegressor(
        n_estimators=200,
        learning_rate=0.1,
        max_depth=3,
        subsample=0.9,
        random_state=random_state,
    )
    model.fit(X_train, y_train)
    y_pred = model.predict(X_test)

    mse = float(mean_squared_error(y_test, y_pred))
    r2 = float(r2_score(y_test, y_pred))
    spearman = _spearman(y_test, y_pred)

    bundle = {
        "model": model,
        "feature_names": list(ACTEURS_FEATURE_NAMES),
        "version": VERSION_ACTEURS,
    }
    metrics = {
        "mse": round(mse, 4),
        "r2": round(r2, 4),
        "spearman": round(spearman, 4),
        "n_entrainement": int(len(X_train)),
        "n_test": int(len(X_test)),
    }
    return bundle, metrics


def _spearman(y_true: np.ndarray, y_pred: np.ndarray) -> float:
    try:
        from scipy.stats import spearmanr

        corr, _ = spearmanr(y_true, y_pred)
        return float(corr)
    except Exception:
        rt = np.argsort(np.argsort(y_true)).astype(float)
        rp = np.argsort(np.argsort(y_pred)).astype(float)
        if rt.std() < 1e-12 or rp.std() < 1e-12:
            return 0.0
        return float(np.corrcoef(rt, rp)[0, 1])


def _raisons(distance: float, note: float, nb_avis: int, disponible: bool,
             match: float, meme_ville: bool, categorie: str) -> list[str]:
    raisons = []
    if distance < 30:
        raisons.append(f"Tout près : {distance:.1f} km")
    else:
        raisons.append(f"Distance de {distance:.1f} km")
    if note >= 4.5:
        raisons.append(f"Excellente note ({note:.1f}/5)")
    elif note >= 3.5:
        raisons.append(f"Bonne note ({note:.1f}/5)")
    else:
        raisons.append(f"Note moyenne ({note:.1f}/5)")
    if nb_avis > 0:
        raisons.append(f"{nb_avis} avis enregistrés")
    raisons.append("Disponible actuellement" if disponible else "Indisponible actuellement")
    if match:
        raisons.append(f"Compétences correspondant à la catégorie {categorie}")
    if meme_ville:
        raisons.append("Même ville que la demande")
    return raisons


def predict(bundle: dict, contexte: dict, candidats: list[dict],
            position: dict, categorie: str) -> dict:
    model: GradientBoostingRegressor = bundle["model"]
    lat = float(position.get("latitude", 0.0))
    lon = float(position.get("longitude", 0.0))
    classement = []
    for candidat in candidats:
        distance = haversine_km(
            lat, lon,
            float(candidat.get("latitude", 0.0)),
            float(candidat.get("longitude", 0.0)),
        )
        note = float(candidat.get("noteMoyenne", 0.0) or 0.0)
        nb_avis = int(candidat.get("nbAvis", 0) or 0)
        disponible = bool(candidat.get("disponible", False))
        match = match_categorie(categorie, candidat.get("competences"), candidat.get("categories"))
        meme_ville = float(contexte.get("memeVille", 0.0) or 0.0)
        if contexte.get("ville") and candidat.get("ville"):
            meme_ville = 1.0 if str(contexte["ville"]).strip().lower() == str(candidat["ville"]).strip().lower() else 0.0

        vector = build_acteurs_features(distance, note, nb_avis, disponible, match, meme_ville)
        proba = float(np.clip(model.predict(vector.reshape(1, -1))[0], 0.0, 1.0))
        note_norm = min(5.0, max(0.0, note)) / 5.0
        delai = round(1.0 + distance / 80.0 + (1.0 - note_norm) * 3.0, 1)
        classement.append(
            {
                "candidatId": candidat.get("id"),
                "score": round(proba, 4),
                "probabiliteAcceptation": round(proba, 4),
                "delaiJoursEstime": delai,
                "distanceKm": round(distance, 2),
                "raisons": _raisons(distance, note, nb_avis, disponible, match, bool(meme_ville), categorie),
            }
        )
    classement.sort(key=lambda c: -c["score"])
    return classement
