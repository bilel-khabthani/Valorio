import numpy as np
from app.ml.features import build_valorisation_features

from training import generate_seed_data
from training.train import train_acteurs, train_valorisation


def test_valorisation_smoke_train_accuracy_sup_05():
    df = generate_seed_data.generate_valorisation_dataset(n=1500, seed=7)
    bundle, metrics, top_features = train_valorisation(df, random_state=7)
    assert metrics["accuracy"] > 0.5, metrics
    assert set(bundle["classes"]) <= {"REPARATION", "DON", "RECYCLAGE", "REVENTE"}
    assert len(top_features) == len(bundle["feature_names"])

    vec = build_valorisation_features("Electronique", "ENDOMMAGE", 3, 650, 400, 1.0)
    proba = bundle["gbm"].predict_proba(vec.reshape(1, -1))[0]
    assert abs(proba.sum() - 1.0) < 1e-6
    assert bundle["lr"].coef_.shape[1] == len(bundle["feature_names"])


def test_acteurs_smoke_train_metrics():
    df = generate_seed_data.generate_acteurs_dataset(n=2500, seed=8)
    bundle, metrics = train_acteurs(df, random_state=8)
    assert metrics["mse"] < 0.06, metrics
    assert metrics["r2"] > 0.3, metrics
    assert -1.0 <= metrics["spearman"] <= 1.0
    assert metrics["r2"] >= metrics["spearman"] - 1.0
