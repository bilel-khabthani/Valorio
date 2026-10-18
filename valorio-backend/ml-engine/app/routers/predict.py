from fastapi import APIRouter

from app.ml import acteurs_model, marche_model, registry, valorisation_model
from app.schemas import (
    ActeurClasse,
    ActeursRequest,
    ArticleClasse,
    MarcheRequest,
    RecoActeursResponse,
    RecoMarcheResponse,
    RecoValorisationResponse,
    ValorisationRequest,
)

router = APIRouter(prefix="/predict", tags=["predict"])


@router.post("/valorisation", response_model=RecoValorisationResponse)
def predict_valorisation(request: ValorisationRequest) -> RecoValorisationResponse:
    bundle = registry.get_bundle()
    with registry.LOCK:
        result = valorisation_model.predict(bundle["valorisation"], request.model_dump())
        version = bundle["valorisation"]["version"]
    return RecoValorisationResponse(modeleVersion=version, **result)


@router.post("/acteurs", response_model=RecoActeursResponse)
def predict_acteurs(request: ActeursRequest) -> RecoActeursResponse:
    bundle = registry.get_bundle()
    contexte = request.contexte.model_dump()
    categorie = contexte.get("categorie") or "Autre"
    candidats = [c.model_dump() for c in request.candidats]
    with registry.LOCK:
        classement = acteurs_model.predict(
            bundle["acteurs"],
            contexte,
            candidats,
            request.position.model_dump(),
            categorie,
        )
        version = bundle["acteurs"]["version"]
    return RecoActeursResponse(
        classement=[ActeurClasse(**c) for c in classement],
        modeleVersion=version,
    )


@router.post("/marche", response_model=RecoMarcheResponse)
def predict_marche(request: MarcheRequest) -> RecoMarcheResponse:
    bundle = registry.get_bundle()
    with registry.LOCK:
        articles = marche_model.predict(
            bundle["marche"],
            request.requete.model_dump(),
            [c.model_dump() for c in request.candidats],
            request.topK,
        )
        version = bundle["marche"]["version"]
    return RecoMarcheResponse(
        articles=[ArticleClasse(**a) for a in articles],
        modeleVersion=version,
    )
