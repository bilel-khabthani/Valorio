"""Modèles Pydantic alignés sur le contrat recommandation.openapi.yaml."""
from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field

RouteValorisation = Literal["REPARATION", "DON", "RECYCLAGE", "REVENTE"]
TypeActeur = Literal["REPARATEUR", "BENEFICIAIRE", "CENTRE_RECYCLAGE"]
EquitePrix = Literal["EXCELLENT_DEAL", "FAIR_MARKET", "OVERPRICED", "UNDERPRICED"]


class ValorisationRequest(BaseModel):
    categorie: str | None = None
    etat: str
    ageAnnees: float = Field(ge=0)
    valeurEstimee: float = Field(ge=0)
    coutReparationEstime: float | None = Field(default=None, ge=0)
    materiaux: list[str] | None = None
    tauxRecyclabilite: float | None = Field(default=None, ge=0, le=1)


class AlternativeReco(BaseModel):
    route: RouteValorisation
    probabilite: float = Field(ge=0, le=1)


class RaisonReco(BaseModel):
    feature: str
    valeur: str
    contribution: float
    libelle: str


class RecoValorisationResponse(BaseModel):
    routeRecommandee: RouteValorisation
    probabilite: float = Field(ge=0, le=1)
    alternatives: list[AlternativeReco]
    raisons: list[RaisonReco]
    confidenceScore: float = Field(ge=0, le=1)
    modeleVersion: str
    source: Literal["ML"] = "ML"


class ContexteActeur(BaseModel):
    categorie: str | None = None
    etat: str | None = None
    ageAnnees: float | None = None
    valeurEstimee: float | None = None
    ville: str | None = None
    rayonKm: float | None = 100


class CandidatActeur(BaseModel):
    id: int
    ville: str | None = None
    latitude: float
    longitude: float
    noteMoyenne: float = 0
    nbAvis: int = 0
    disponible: bool = False
    competences: str | None = None
    categories: str | None = None


class Position(BaseModel):
    latitude: float
    longitude: float


class ActeursRequest(BaseModel):
    typeActeur: TypeActeur
    contexte: ContexteActeur = Field(default_factory=ContexteActeur)
    candidats: list[CandidatActeur]
    position: Position


class ActeurClasse(BaseModel):
    candidatId: int
    score: float = Field(ge=0, le=1)
    probabiliteAcceptation: float = Field(ge=0, le=1)
    delaiJoursEstime: float = Field(ge=0)
    distanceKm: float = Field(ge=0)
    raisons: list[str]


class RecoActeursResponse(BaseModel):
    classement: list[ActeurClasse]
    modeleVersion: str
    source: Literal["ML"] = "ML"


class ArticleMarche(BaseModel):
    id: int | None = None
    titre: str
    description: str | None = None
    categorie: str | None = None
    etat: str | None = None
    prix: float | None = None


class MarcheRequest(BaseModel):
    requete: ArticleMarche
    candidats: list[ArticleMarche] = Field(default_factory=list)
    topK: int = Field(default=5, ge=1, le=20)


class ArticleClasse(BaseModel):
    articleId: int | None
    titre: str
    similarite: float = Field(ge=0, le=1)
    scoreDeal: float = Field(ge=0, le=1)
    prix: float
    prixRecommande: float
    equitePrix: EquitePrix
    raisons: list[str]


class RecoMarcheResponse(BaseModel):
    articles: list[ArticleClasse]
    modeleVersion: str
    source: Literal["ML"] = "ML"


class ModeleMetadata(BaseModel):
    actif: bool = True
    source: Literal["ML"] = "ML"
    versionValorisation: str
    versionActeurs: str
    versionMarche: str
    metriques: dict[str, float]
    topFeatures: list[str]
