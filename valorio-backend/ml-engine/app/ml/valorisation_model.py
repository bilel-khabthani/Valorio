"""Modèle de recommandation de route de valorisation (GradientBoosting multi-classes)."""
from __future__ import annotations

import math

import numpy as np
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression

from app.ml.features import (
    VALORISATION_FEATURE_NAMES,
    build_valorisation_features,
    libelle_feature,
    normalize_categorie,
    normalize_etat,
    taux_recyclabilite,
)

VERSION_VALORISATION = "valorisation-gbm-1.0.0"
ROUTES = ["REPARATION", "DON", "RECYCLAGE", "REVENTE"]


def train_bundle(X: np.ndarray, y: np.ndarray, random_state: int = 42) -> dict:
    """Entraîne le GBM (prédiction) et la régression logistique (classement des features)."""
    from sklearn.metrics import accuracy_score, f1_score
    from sklearn.model_selection import train_test_split

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=random_state, stratify=y
    )
    gbm = GradientBoostingClassifier(
        n_estimators=200,
        learning_rate=0.08,
        max_depth=3,
        subsample=0.9,
        random_state=random_state,
    )
    gbm.fit(X_train, y_train)
    lr = LogisticRegression(max_iter=2000, random_state=random_state)
    lr.fit(X_train, y_train)

    y_pred = gbm.predict(X_test)
    accuracy = float(accuracy_score(y_test, y_pred))
    f1_macro = float(f1_score(y_test, y_pred, average="macro"))

    importances = np.abs(lr.coef_).mean(axis=0)
    top_features = [
        name
        for _, name in sorted(
            zip(importances, VALORISATION_FEATURE_NAMES), key=lambda p: -p[0]
        )
    ]

    bundle = {
        "gbm": gbm,
        "lr": lr,
        "feature_names": list(VALORISATION_FEATURE_NAMES),
        "classes": list(gbm.classes_),
        "version": VERSION_VALORISATION,
    }
    metrics = {
        "accuracy": round(accuracy, 4),
        "f1_macro": round(f1_macro, 4),
        "n_entrainement": int(len(X_train)),
        "n_test": int(len(X_test)),
    }
    return bundle, metrics, top_features


def build_vector(payload: dict) -> tuple[np.ndarray, dict]:
    categorie = normalize_categorie(payload.get("categorie"))
    etat = normalize_etat(payload.get("etat"))
    age = max(0.0, float(payload.get("ageAnnees", 0.0) or 0.0))
    valeur = max(0.0, float(payload.get("valeurEstimee", 0.0) or 0.0))
    cout = max(0.0, float(payload.get("coutReparationEstime", 0.0) or 0.0))
    taux = payload.get("tauxRecyclabilite")
    if taux is None:
        taux = taux_recyclabilite(payload.get("materiaux"))
    taux = min(1.0, max(0.0, float(taux)))
    vector = build_valorisation_features(categorie, etat, age, valeur, cout, taux)
    ctx = {
        "categorie": categorie,
        "etat": etat,
        "ageAnnees": age,
        "valeurEstimee": valeur,
        "coutReparationEstime": cout,
        "tauxRecyclabilite": taux,
    }
    return vector, ctx


def _confidence(probas: np.ndarray) -> float:
    p = np.clip(probas.astype(float), 1e-9, 1.0)
    entropy = float(-(p * np.log(p)).sum())
    max_entropy = math.log(len(p)) if len(p) > 1 else 1.0
    ent_conf = max(0.0, 1.0 - entropy / max_entropy)
    return round(min(1.0, 0.5 * float(p.max()) + 0.5 * ent_conf), 4)


def _explanations(vector: np.ndarray, ctx: dict, bundle: dict, route: str) -> list[dict]:
    lr: LogisticRegression = bundle["lr"]
    classes = list(bundle["classes"])
    idx = classes.index(route) if route in classes else 0
    coefs = lr.coef_[idx]
    contribs = coefs * vector
    total = float(np.abs(contribs).sum())
    if total > 0:
        contribs = contribs / total
    order = np.argsort(-np.abs(contribs))[:5]
    raisons = []
    for i in order:
        name = bundle["feature_names"][int(i)]
        valeur = vector[int(i)]
        if name == "logValeur":
            valeur_str = f"{ctx['valeurEstimee']:g}"
        elif name == "logCout":
            valeur_str = f"{ctx['coutReparationEstime']:g}"
        elif name == "tauxRecyclabilite":
            valeur_str = f"{ctx['tauxRecyclabilite']:.2f}"
        elif name.startswith("cat_"):
            valeur_str = name[4:]
        elif name == "etatOrdinal":
            valeur_str = ctx["etat"]
        else:
            valeur_str = f"{valeur:g}"
        raisons.append(
            {
                "feature": name,
                "valeur": valeur_str,
                "contribution": round(float(contribs[int(i)]), 4),
                "libelle": libelle_feature(name, ctx),
            }
        )
    return raisons


def predict(bundle: dict, payload: dict) -> dict:
    gbm: GradientBoostingClassifier = bundle["gbm"]
    vector, ctx = build_vector(payload)
    proba = gbm.predict_proba(vector.reshape(1, -1))[0]
    classes = list(bundle["classes"])
    ranking = sorted(zip(classes, proba), key=lambda p: -p[1])
    best_route, best_proba = ranking[0]
    alternatives = [
        {"route": r, "probabilite": round(float(p), 4)} for r, p in ranking[1:]
    ]
    return {
        "routeRecommandee": best_route,
        "probabilite": round(float(best_proba), 4),
        "alternatives": alternatives,
        "raisons": _explanations(vector, ctx, bundle, best_route),
        "confidenceScore": _confidence(proba),
    }
