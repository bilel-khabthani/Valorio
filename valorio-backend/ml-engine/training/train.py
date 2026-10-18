"""Entraînement CLI : python -m training.train → artefacts models/ + metrics."""
from __future__ import annotations

import sys
from pathlib import Path

import numpy as np
import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from app.ml import acteurs_model, marche_model, registry, valorisation_model  # noqa: E402
from app.ml.features import (  # noqa: E402
    ACTEURS_FEATURE_NAMES,
    VALORISATION_FEATURE_NAMES,
    build_acteurs_features,
    build_valorisation_features,
    taux_recyclabilite,
)
from training import generate_seed_data  # noqa: E402


def _valorisation_xy(df: pd.DataFrame) -> tuple[np.ndarray, np.ndarray]:
    vectors = [
        build_valorisation_features(
            row.categorie,
            row.etat,
            row.ageAnnees,
            row.valeurEstimee,
            row.coutReparationEstime,
            float(row.tauxRecyclabilite)
            if pd.notna(row.tauxRecyclabilite)
            else taux_recyclabilite(row.materiaux),
        )
        for row in df.itertuples()
    ]
    return np.vstack(vectors), df["route"].to_numpy()


def _acteurs_xy(df: pd.DataFrame) -> tuple[np.ndarray, np.ndarray]:
    vectors = [
        build_acteurs_features(
            row.distanceKm, row.noteMoyenne, row.nbAvis, bool(row.disponible),
            row.matchCategorie, row.memeVille,
        )
        for row in df.itertuples()
    ]
    return np.vstack(vectors), df["probaAcceptation"].to_numpy()


def train_valorisation(df: pd.DataFrame, random_state: int = 42):
    X, y = _valorisation_xy(df)
    return valorisation_model.train_bundle(X, y, random_state=random_state)


def train_acteurs(df: pd.DataFrame, random_state: int = 42):
    X, y = _acteurs_xy(df)
    return acteurs_model.train_bundle(X, y, random_state=random_state)


def build_metadata(valorisation_metrics: dict, acteurs_metrics: dict,
                   marche_metrics: dict, top_features: list[str]) -> dict:
    metriques = {
        "accuracy": float(valorisation_metrics["accuracy"]),
        "f1_macro": float(valorisation_metrics["f1_macro"]),
        "acteurs_mse": float(acteurs_metrics["mse"]),
        "acteurs_r2": float(acteurs_metrics["r2"]),
        "acteurs_spearman": float(acteurs_metrics["spearman"]),
        "marche_documents": float(marche_metrics["corpus_documents"]),
    }
    return {
        "actif": True,
        "source": "ML",
        "versionValorisation": valorisation_model.VERSION_VALORISATION,
        "versionActeurs": acteurs_model.VERSION_ACTEURS,
        "versionMarche": marche_model.VERSION_MARCHE,
        "metriques": metriques,
        "topFeatures": top_features[:10],
    }


def train_all(write_csvs: bool = True, seed: int = 42) -> dict:
    """Entraîne les trois modèles et sauvegarde les artefacts via le registry."""
    if write_csvs:
        generate_seed_data.write_seed_csvs(seed=seed)
        valo_df = pd.read_csv(generate_seed_data.DATA_DIR / "valorisation_seed.csv")
        act_df = pd.read_csv(generate_seed_data.DATA_DIR / "acteurs_seed.csv")
        marche_df = pd.read_csv(generate_seed_data.DATA_DIR / "marche_corpus.csv")
    else:
        valo_df = generate_seed_data.generate_valorisation_dataset(seed=seed)
        act_df = generate_seed_data.generate_acteurs_dataset(seed=seed + 1)
        marche_df = generate_seed_data.generate_marche_corpus(seed=seed + 2)

    valo_bundle, valo_metrics, top_features = train_valorisation(valo_df, random_state=seed)
    act_bundle, act_metrics = train_acteurs(act_df, random_state=seed)
    corpus = marche_df.to_dict("records")
    marche_bundle = marche_model.train_bundle(corpus)
    marche_metrics = {"corpus_documents": float(len(corpus))}

    metadata = build_metadata(valo_metrics, act_metrics, marche_metrics, top_features)
    registry.save_artifacts(valo_bundle, act_bundle, marche_bundle, metadata)
    return metadata


def main() -> None:
    print("Génération des données de grain + entraînement des modèles…")
    metadata = train_all(write_csvs=True)
    m = metadata["metriques"]
    print()
    print(f"[VALORISATION] accuracy={m['accuracy']:.4f}  f1_macro={m['f1_macro']:.4f}"
          f"  (split 80/20, random_state=42)")
    print(f"[ACTEURS]      mse={m['acteurs_mse']:.4f}  r2={m['acteurs_r2']:.4f}"
          f"  spearman={m['acteurs_spearman']:.4f}")
    print(f"[MARCHE]       corpus={int(m['marche_documents'])} documents (TF-IDF, refit par requête)")
    print(f"[TOP FEATURES] {', '.join(metadata['topFeatures'][:8])}")
    print()
    print(f"Artefacts écrits dans {registry.MODELS_DIR}")
    print(f"  metadata: {registry.METADATA_PATH}")


if __name__ == "__main__":
    main()
