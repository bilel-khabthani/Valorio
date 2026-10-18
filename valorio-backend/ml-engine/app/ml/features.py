"""Extraction et encodage des features partagés par les modèles ML de Valorio."""
from __future__ import annotations

import math

import numpy as np

CATEGORIES = ["Electronique", "Meubles", "Velo & mobilite", "Vetements", "Cuisine", "Autre"]
ETATS = ["NEUF", "EXCELLENT", "BON", "ENDOMMAGE", "HORS_SERVICE"]
ETAT_ORDINAL = {"NEUF": 0, "EXCELLENT": 1, "BON": 2, "ENDOMMAGE": 3, "HORS_SERVICE": 4}
DEFAULT_CATEGORIE = "Autre"
DEFAULT_ETAT = "BON"

MATERIAUX_VALORISABLES = {
    "plastique",
    "aluminium",
    "acier",
    "verre",
    "bois",
    "papier",
    "carton",
    "caoutchouc",
    "cuivre",
    "lithium",
}

BENCHMARKS_EUR = {
    "Electronique": 250.0,
    "Meubles": 60.0,
    "Velo & mobilite": 180.0,
    "Vetements": 45.0,
    "Cuisine": 50.0,
    "Autre": 30.0,
}

CONDITION_FACTORS = {
    "NEUF": 1.0,
    "EXCELLENT": 0.9,
    "BON": 0.7,
    "ENDOMMAGE": 0.35,
    "HORS_SERVICE": 0.15,
}

CATEGORIE_MOTS_CLES = {
    "Electronique": [
        "electronique", "electroniques", "telephone", "telephones", "ordinateur",
        "ordinateurs", "smartphone", "ecran", "tablette", "audio", "camera",
        "informatique", "téléphone", "électronique", "écran",
    ],
    "Meubles": [
        "meuble", "meubles", "bois", "menuiserie", "menuisier", "table", "chaise",
        "armoire", "buffet", "ébénisterie", "ebenisterie",
    ],
    "Velo & mobilite": [
        "velo", "velos", "vélo", "vélos", "mobilite", "mobilite", "mobilité",
        "bicyclette", "scooter", "moto", "roue", "velross", "trottinette",
    ],
    "Vetements": [
        "vetement", "vetements", "vêtement", "vêtements", "couture", "textile",
        "tissu", "mode", "cousu", "pull", "manteau",
    ],
    "Cuisine": [
        "cuisine", "cuisson", "electromenager", "électroménager", "ustensile",
        "ustensiles", "casserole", "four", "mijoteuse", "robot",
    ],
    "Autre": [],
}

VALORISATION_FEATURE_NAMES = [f"cat_{c}" for c in CATEGORIES] + [
    "etatOrdinal",
    "ageAnnees",
    "logValeur",
    "logCout",
    "tauxRecyclabilite",
    "coutRatio",
    "age_x_etat",
]

ACTEURS_FEATURE_NAMES = [
    "distanceKm",
    "noteNorm",
    "logNbAvis",
    "disponible",
    "matchCategorie",
    "memeVille",
]

FRENCH_STOP_WORDS = [
    "au", "aux", "avec", "ce", "ces", "dans", "de", "des", "du", "elle", "elles",
    "en", "et", "eux", "il", "ils", "je", "la", "le", "les", "leur", "lui", "ma",
    "mais", "me", "meme", "même", "mes", "moi", "mon", "ne", "nos", "notre",
    "nous", "on", "ou", "par", "pas", "pour", "qu", "que", "qui", "sa", "se",
    "ses", "son", "sont", "sur", "ta", "te", "tes", "toi", "ton", "tu", "un",
    "une", "vos", "votre", "vous", "suis", "es", "est", "sommes", "etes", "êtes",
    "a", "ai", "ont", "ont", "fait", "faire", "plus", "tres", "très", "tout",
    "tous", "toute", "toutes", "autre", "autres", "encore", "deja", "déjà",
    "comme", "si", "sans", "sous", "entre", "apres", "après", "avant", "chez",
    "vers", "mais", "donc", "car", "or", "ni", "quelque", "quelques", "chaque",
    "ceux", "celui", "celle", "soit", "quand", "quoi", "comment", "pourquoi",
    "quel", "quelle", "meme", "afin", "lors", "des", "dux", "etait", "était",
    "etre", "être", "etaient", "étaient", "aurait", "avait", "avoir", "etres",
]


def normalize_categorie(categorie: str | None) -> str:
    if categorie and categorie in CATEGORIES:
        return categorie
    return DEFAULT_CATEGORIE


def normalize_etat(etat: str | None) -> str:
    if etat and etat in ETAT_ORDINAL:
        return etat
    return DEFAULT_ETAT


def taux_recyclabilite(materiaux: list[str] | None) -> float:
    if not materiaux:
        return 0.0
    propres = [str(m).strip().lower() for m in materiaux if str(m).strip()]
    if not propres:
        return 0.0
    valorisables = sum(1 for m in propres if m in MATERIAUX_VALORISABLES)
    return valorisables / len(propres)


def haversine_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    r = 6371.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp = math.radians(lat2 - lat1)
    dl = math.radians(lon2 - lon1)
    a = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


def match_categorie(categorie: str, competences: str | None = "", categories: str | None = "") -> float:
    texte = f"{competences or ''} {categories or ''}".lower()
    if not texte.strip():
        return 0.0
    mots = CATEGORIE_MOTS_CLES.get(categorie, [])
    return 1.0 if any(m in texte for m in mots) else 0.0


def build_valorisation_features(
    categorie: str,
    etat: str,
    age_annees: float,
    valeur_estimee: float,
    cout_reparation: float,
    taux: float,
) -> np.ndarray:
    cat = normalize_categorie(categorie)
    et = normalize_etat(etat)
    age = max(0.0, float(age_annees))
    valeur = max(0.0, float(valeur_estimee))
    cout = max(0.0, float(cout_reparation))
    taux = min(1.0, max(0.0, float(taux)))
    etat_ord = float(ETAT_ORDINAL[et])
    cout_ratio = cout / max(valeur, 1.0)

    one_hot = [1.0 if c == cat else 0.0 for c in CATEGORIES]
    vector = one_hot + [
        etat_ord,
        age,
        float(np.log1p(valeur)),
        float(np.log1p(cout)),
        taux,
        cout_ratio,
        age * etat_ord,
    ]
    return np.asarray(vector, dtype=float)


def build_acteurs_features(
    distance_km: float,
    note_moyenne: float,
    nb_avis: int,
    disponible: bool,
    match_cat: float,
    meme_ville: float,
) -> np.ndarray:
    note_norm = min(5.0, max(0.0, float(note_moyenne))) / 5.0
    return np.asarray(
        [
            max(0.0, float(distance_km)),
            note_norm,
            float(np.log1p(max(0, int(nb_avis)))),
            1.0 if disponible else 0.0,
            1.0 if match_cat else 0.0,
            1.0 if meme_ville else 0.0,
        ],
        dtype=float,
    )


def libelle_feature(name: str, ctx: dict) -> str:
    categorie = ctx.get("categorie", DEFAULT_CATEGORIE)
    etat = ctx.get("etat", DEFAULT_ETAT)
    age = float(ctx.get("ageAnnees", 0.0) or 0.0)
    valeur = float(ctx.get("valeurEstimee", 0.0) or 0.0)
    cout = float(ctx.get("coutReparationEstime", 0.0) or 0.0)
    taux = float(ctx.get("tauxRecyclabilite", 0.0) or 0.0)
    ratio = cout / max(valeur, 1.0)
    if name.startswith("cat_"):
        return f"Catégorie de l'objet : {name[4:]}"
    if name == "etatOrdinal":
        return f"État de l'objet : {etat}"
    if name == "ageAnnees":
        return f"Âge de l'objet : {age:g} an(s)"
    if name == "logValeur":
        return f"Valeur estimée : {valeur:g} €"
    if name == "logCout":
        return f"Coût de réparation estimé : {cout:g} €"
    if name == "tauxRecyclabilite":
        return f"Taux de recyclabilité : {taux:.0%} (catégorie {categorie})"
    if name == "coutRatio":
        return f"Ratio coût de réparation / valeur : {ratio:.2f}"
    if name == "age_x_etat":
        return f"Usure combinée âge × état : {age:g} × {ETAT_ORDINAL.get(etat, 2)}"
    return name
