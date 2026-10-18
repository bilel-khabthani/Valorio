from fastapi import APIRouter

from app.ml import registry
from app.schemas import ModeleMetadata

router = APIRouter(prefix="/model", tags=["modele"])


@router.get("/metadata", response_model=ModeleMetadata)
def metadata() -> ModeleMetadata:
    meta = registry.get_metadata()
    return ModeleMetadata(**meta)
