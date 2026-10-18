"""Modèle marché : similarité TF-IDF + équité de prix (miroir du module Marketplace)."""
from __future__ import annotations

import unicodedata

import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

from app.ml.features import (
    BENCHMARKS_EUR,
    CONDITION_FACTORS,
    FRENCH_STOP_WORDS,
    normalize_categorie,
    normalize_etat,
)

VERSION_MARCHE = "marche-tfidf-1.0.1"


def train_bundle(corpus: list[dict] | None = None) -> dict:
    """Pas d'entraînement lourd : mémorise la version et un corpus de référence."""
    return {"version": VERSION_MARCHE, "corpus": corpus or []}


def _normaliser(texte: str) -> str:
    decompose = unicodedata.normalize("NFKD", texte or "")
    sans_accents = "".join(c for c in decompose if not unicodedata.combining(c))
    return sans_accents.lower()


def _doc(article: dict) -> str:
    brut = f"{article.get('titre', '')} {article.get('description', '')}".strip()
    return _normaliser(brut)


def prix_recommande(categorie: str | None, etat: str | None, age_annees: float = 0.0) -> float:
    bench = BENCHMARKS_EUR.get(normalize_categorie(categorie), BENCHMARKS_EUR["Autre"])
    factor = CONDITION_FACTORS.get(normalize_etat(etat), CONDITION_FACTORS["BON"])
    age = max(0.0, float(age_annees or 0.0))
    prix = bench * factor * (1.0 - min(0.5, 0.07 * age))
    return max(1.0, round(prix, 2))


def _clip01(x: float) -> float:
    return float(min(1.0, max(0.0, x)))


def equite_prix(prix: float, prix_rec: float) -> str:
    if prix is None or prix <= 0:
        return "EXCELLENT_DEAL"
    ratio = prix / max(prix_rec, 1.0)
    if ratio <= 0.7:
        return "EXCELLENT_DEAL"
    if ratio <= 1.25:
        return "FAIR_MARKET"
    return "OVERPRICED"


def _raisons(sim: float, prix: float, prix_rec: float, equite: str, etat: str) -> list[str]:
    raisons = [f"Similarité de contenu : {sim:.0%}"]
    if prix <= 0:
        raisons.append("Objet proposé gratuitement (don)")
    elif equite == "EXCELLENT_DEAL":
        raisons.append(f"Prix {prix:.0f} € très inférieur au prix recommandé ({prix_rec:.0f} €)")
    elif equite == "FAIR_MARKET":
        raisons.append(f"Prix {prix:.0f} € équitable (recommandé : {prix_rec:.0f} €)")
    else:
        raisons.append(f"Prix {prix:.0f} € supérieur au prix recommandé ({prix_rec:.0f} €)")
    if etat in ("NEUF", "EXCELLENT"):
        raisons.append(f"État {etat} — forte valeur de revente")
    return raisons


def predict(bundle: dict, requete: dict, candidats: list[dict], top_k: int = 5) -> list[dict]:
    if not candidats:
        return []
    docs = [_doc(requete)] + [_doc(c) for c in candidats]
    try:
        vectorizer = TfidfVectorizer(min_df=1, stop_words=FRENCH_STOP_WORDS)
        matrix = vectorizer.fit_transform(docs)
    except ValueError:
        matrix = np.zeros((len(docs), 1))
    sims = cosine_similarity(matrix[0:1], matrix[1:])[0]

    articles = []
    for candidat, sim in zip(candidats, sims):
        prix = float(candidat.get("prix", 0.0) or 0.0)
        prix_rec = prix_recommande(candidat.get("categorie") or requete.get("categorie"),
                                   candidat.get("etat") or requete.get("etat"), 0.0)
        deal = _clip01(1.0 - prix / max(prix_rec, 1.0))
        score = 0.6 * deal + 0.4 * float(sim)
        equite = equite_prix(prix, prix_rec)
        articles.append(
            {
                "articleId": candidat.get("id"),
                "titre": candidat.get("titre", ""),
                "similarite": round(float(sim), 4),
                "scoreDeal": round(_clip01(score), 4),
                "prix": prix,
                "prixRecommande": prix_rec,
                "equitePrix": equite,
                "raisons": _raisons(float(sim), prix, prix_rec, equite, str(candidat.get("etat") or "")),
            }
        )
    articles.sort(key=lambda a: -a["scoreDeal"])
    return articles[: max(1, int(top_k))]
