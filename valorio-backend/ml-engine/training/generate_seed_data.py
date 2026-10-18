"""Générateur de données d'étiquetage synthétiques et déterministes (RNG seedé)."""
from __future__ import annotations

from pathlib import Path

import numpy as np
import pandas as pd

from app.ml.features import CATEGORIE_MOTS_CLES, haversine_km, taux_recyclabilite

ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "data"

VALORISATION_N = 6000
ACTEURS_N = 8000
MARCHE_N = 120

CATEGORIES_POOL = ["Electronique", "Meubles", "Velo & mobilite", "Vetements", "Cuisine", "Autre"]
CATEGORIES_P = [0.22, 0.18, 0.15, 0.20, 0.15, 0.10]
ETATS_POOL = ["NEUF", "EXCELLENT", "BON", "ENDOMMAGE", "HORS_SERVICE"]
ETATS_P = [0.12, 0.20, 0.30, 0.28, 0.10]

AGE_MEAN = {"NEUF": 0.6, "EXCELLENT": 1.8, "BON": 4.0, "ENDOMMAGE": 4.5, "HORS_SERVICE": 8.0}
AGE_SD = {"NEUF": 0.4, "EXCELLENT": 0.9, "BON": 1.8, "ENDOMMAGE": 2.4, "HORS_SERVICE": 2.5}
VALUE_FACTOR = {"NEUF": 1.1, "EXCELLENT": 0.85, "BON": 0.5, "ENDOMMAGE": 0.35, "HORS_SERVICE": 0.12}

MATERIALS_BY_CAT = {
    "Electronique": ["plastique", "aluminium", "verre", "lithium", "cuivre"],
    "Meubles": ["bois", "plastique", "acier"],
    "Velo & mobilite": ["acier", "caoutchouc", "aluminium", "plastique"],
    "Vetements": ["tissu", "tissu", "carton", "plastique"],
    "Cuisine": ["acier", "verre", "plastique", "caoutchouc"],
    "Autre": ["plastique", "carton", "papier", "bois", "tissu"],
}

VILLES = [
    ("Tunis", 36.8065, 10.1815),
    ("Sfax", 34.7406, 10.7603),
    ("Sousse", 35.8256, 10.6084),
    ("Kairouan", 35.6781, 10.0963),
    ("Bizerte", 37.2744, 9.8739),
    ("Nabeul", 36.4561, 10.7376),
    ("Gabes", 33.8815, 10.0982),
    ("Ariana", 36.8625, 10.1956),
]

LABELS = ["REPARATION", "DON", "RECYCLAGE", "REVENTE"]


def cdc_label(etat: str, age: float, valeur: float, cout: float, taux: float, rng) -> str:
    """Règles de classement de la CDC (comité de direction) pour une route de valorisation."""
    if etat in ("NEUF", "EXCELLENT"):
        if age <= 2.0 and valeur >= 150.0:
            return "REVENTE"
        ratio = cout / max(valeur, 1.0)
        if cout > 0 and ratio <= 0.3 and rng.random() < 0.15:
            return "REPARATION"
        return "DON"
    if etat == "BON":
        return "REVENTE" if valeur >= 80.0 else "DON"
    if etat == "ENDOMMAGE":
        ratio = cout / max(valeur, 1.0)
        if ratio <= 0.5:
            return "REPARATION"
        if ratio <= 0.8:
            if taux >= 0.5:
                return "REPARATION" if rng.random() < 0.5 else "RECYCLAGE"
            return "REPARATION"
        return "RECYCLAGE" if taux >= 0.5 else "DON"
    return "RECYCLAGE" if taux >= 0.5 else "DON"


def generate_valorisation_dataset(n: int = VALORISATION_N, seed: int = 42) -> pd.DataFrame:
    rng = np.random.default_rng(seed)
    rows = []
    benchmarks = {"Electronique": 250, "Meubles": 60, "Velo & mobilite": 180,
                  "Vetements": 45, "Cuisine": 50, "Autre": 30}
    for _ in range(n):
        categorie = str(rng.choice(CATEGORIES_POOL, p=CATEGORIES_P))
        etat = str(rng.choice(ETATS_POOL, p=ETATS_P))
        age = max(0.0, float(rng.normal(AGE_MEAN[etat], AGE_SD[etat])))
        valeur = benchmarks[categorie] * VALUE_FACTOR[etat] * float(np.exp(rng.normal(0, 0.65)))
        if etat == "ENDOMMAGE":
            cout = valeur * float(rng.uniform(0.1, 1.3))
        elif etat == "HORS_SERVICE":
            cout = valeur * float(rng.uniform(0.2, 1.0))
        elif rng.random() < 0.25:
            cout = valeur * float(rng.uniform(0.02, 0.25))
        else:
            cout = 0.0
        pool = MATERIALS_BY_CAT[categorie]
        k = int(rng.integers(1, 4))
        materiaux = [str(m) for m in rng.choice(pool, size=k, replace=False)]
        taux = taux_recyclabilite(materiaux)

        age = max(0.0, age + float(rng.normal(0, 0.25)))
        valeur = max(1.0, valeur * float(np.exp(rng.normal(0, 0.12))))
        cout = max(0.0, cout * float(np.exp(rng.normal(0, 0.12))))

        route = cdc_label(etat, age, valeur, cout, taux, rng)
        if rng.random() < 0.08:
            autres = [l for l in LABELS if l != route]
            route = str(rng.choice(autres))

        rows.append(
            {
                "categorie": categorie,
                "etat": etat,
                "ageAnnees": round(age, 2),
                "valeurEstimee": round(valeur, 2),
                "coutReparationEstime": round(cout, 2),
                "materiaux": materiaux,
                "tauxRecyclabilite": round(taux, 4),
                "route": route,
            }
        )
    return pd.DataFrame(rows)


def _candidat_text(cat_servies: list[str], rng) -> str:
    parts: list[str] = []
    for c in cat_servies:
        parts.append(c)
        mots = CATEGORIE_MOTS_CLES.get(c, [])
        if mots:
            parts.append(str(rng.choice(mots)))
    if rng.random() < 0.5:
        parts.append(str(rng.choice(["reparation rapide", "diagnostic", "installation", "livraison", "sav"])))
    return ", ".join(parts)


def generate_acteurs_dataset(n: int = ACTEURS_N, seed: int = 43) -> pd.DataFrame:
    rng = np.random.default_rng(seed)
    rows = []
    for _ in range(n):
        categorie = str(rng.choice(CATEGORIES_POOL, p=CATEGORIES_P))
        i_dem = int(rng.integers(len(VILLES)))
        ville_dem, lat_dem, lon_dem = VILLES[i_dem]
        if rng.random() < 0.55:
            i_can = i_dem
        else:
            i_can = int(rng.integers(len(VILLES)))
        ville_can, lat_base, lon_base = VILLES[i_can]
        lat_can = lat_base + float(rng.normal(0, 0.04))
        lon_can = lon_base + float(rng.normal(0, 0.04))
        distance = haversine_km(lat_dem, lon_dem, lat_can, lon_can)

        note = float(np.clip(rng.normal(4.0, 0.75), 1.0, 5.0))
        nb_avis = int(rng.integers(0, 150))
        disponible = int(rng.random() < 0.75)
        meme_ville = int(i_can == i_dem)

        k = int(rng.integers(1, 4))
        servies = [str(c) for c in rng.choice(CATEGORIES_POOL[:5], size=k, replace=False)]
        competences = _candidat_text(servies, rng)
        categories_str = ", ".join(servies)
        from app.ml.features import match_categorie

        match = match_categorie(categorie, competences, categories_str)
        if match == 0.0 and rng.random() < 0.15:
            mots = CATEGORIE_MOTS_CLES.get(categorie, [])
            if mots:
                competences += ", " + str(rng.choice(mots))
                match = match_categorie(categorie, competences, categories_str)

        z = (
            3.0
            - 0.012 * distance
            + 0.9 * (note / 5.0)
            + 0.15 * match
            + 0.4 * disponible
            + 0.2 * meme_ville
            + float(rng.normal(0, 0.35))
        )
        proba = float(np.clip(1.0 / (1.0 + np.exp(-z)), 0.01, 0.99))
        rows.append(
            {
                "categorie": categorie,
                "villeDemande": ville_dem,
                "latitudeDemande": lat_dem,
                "longitudeDemande": lon_dem,
                "ville": ville_can,
                "latitude": lat_can,
                "longitude": lon_can,
                "noteMoyenne": round(note, 2),
                "nbAvis": nb_avis,
                "disponible": disponible,
                "competences": competences,
                "categories": categories_str,
                "matchCategorie": match,
                "memeVille": meme_ville,
                "distanceKm": round(distance, 3),
                "probaAcceptation": round(proba, 4),
            }
        )
    return pd.DataFrame(rows)


TITRES_MARCHE = {
    "Electronique": [
        ("Ordinateur portable - ecran casse", "Portable 15 pouces, ecran fende, fonctionne sous alimentation."),
        ("Smartphone - batterie usee", "Telephone tactile, batterie gonflee, ecran intact."),
        ("Casque audio sans fil", "Casque bluetooth, mousse usee, son correct."),
        ("Tablette tactile - ecran frole", "Tablette 10 pouces, ecran frole dans un coin."),
        ("Appareil photo numerique", "Compact 20 Mpx, objectif raye, viseur OK."),
    ],
    "Meubles": [
        ("Table en bois - pied desserre", "Table a manger en bois massif, un pied a desserrer."),
        ("Chaise de bureau - roulettes cassees", "Chaise ergonomique, deux roulettes cassees."),
        ("Bibliotheque en kit", "Etagere montee, deux plaques manquantes."),
        ("Commode vintage", "Commode 3 tiroirs, vernis ecaille, bonne structure."),
    ],
    "Velo & mobilite": [
        ("Velo de ville - roue voilee", "Velo de ville, roue arriere voilee, freins a reajuster."),
        ("Trottinette electrique - crevaison", "Trottinette pliable, pneu arriere creve, moteur OK."),
        ("Scooter 50cc - demarreur HS", "Scooter urbain, demarreur bloque, carburateur propre."),
        ("Velo de course - chaine tired", "Cadre alu, chaine tired, pneus a changer."),
    ],
    "Vetements": [
        ("Manteau d'hiver - hors d'usage", "Manteau laine, doublure dechiree, hors d'usage."),
        ("Jean usé - genoux percés", "Jean denim, genoux percés, couture a refaire."),
        ("Robe de soirée - fermeture cassee", "Robe longue, fermeture eclair bloque."),
        ("Blouson cuir - manches fatiguées", "Blouson cuir, manches usees, doublure intacte."),
    ],
    "Cuisine": [
        ("Robot pâtissier - moteur bruyant", "Robot pâtissier, moteur bruyant mais fonctionnel."),
        ("Casserole inox - fond bombé", "Casserole inox, fond legerement bombe."),
        ("Four à micro-ondes - plateau fende", "Micro-ondes 800W, plateau tournant fende."),
        ("Poêle anti-adhésive - revêtement usé", "Poêle 26 cm, revêtement use, corps sain."),
    ],
    "Autre": [
        ("Lampe de bureau - interrupteur HS", "Lampe articulee, interrupteur bloque."),
        ("Valise de voyage - roue cassee", "Valise cabine, une roue cassee, serrure OK."),
        ("Miroir de sol - tache argenture", "Miroir pied, tache sur l'argenture en bordure."),
    ],
}


def generate_marche_corpus(n: int = MARCHE_N, seed: int = 44) -> pd.DataFrame:
    from app.ml.features import CONDITION_FACTORS, BENCHMARKS_EUR

    rng = np.random.default_rng(seed)
    rows = []
    for i in range(n):
        categorie = str(rng.choice(CATEGORIES_POOL[:5]))
        etat = str(rng.choice(ETATS_POOL, p=ETATS_P))
        titre, description = TITRES_MARCHE[categorie][int(rng.integers(len(TITRES_MARCHE[categorie])))]
        prix = (
            BENCHMARKS_EUR[categorie]
            * CONDITION_FACTORS[etat]
            * float(rng.uniform(0.7, 1.4))
        )
        rows.append(
            {
                "id": i + 1,
                "titre": titre,
                "description": description,
                "categorie": categorie,
                "etat": etat,
                "prix": round(max(0.0, prix), 2),
            }
        )
    return pd.DataFrame(rows)


def write_seed_csvs(seed: int = 42) -> dict[str, Path]:
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    paths = {}
    for name, df in (
        ("valorisation_seed.csv", generate_valorisation_dataset(seed=seed)),
        ("acteurs_seed.csv", generate_acteurs_dataset(seed=seed + 1)),
        ("marche_corpus.csv", generate_marche_corpus(seed=seed + 2)),
    ):
        path = DATA_DIR / name
        df.to_csv(path, index=False, encoding="utf-8")
        paths[name] = path
    return paths


if __name__ == "__main__":
    for nom, chemin in write_seed_csvs().items():
        print(f"{nom} -> {chemin}")
