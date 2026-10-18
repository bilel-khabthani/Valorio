from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.ml import registry
from app.routers import model, predict


@asynccontextmanager
async def lifespan(_: FastAPI):
    registry.ensure_models()
    yield


app = FastAPI(title="ml-engine Valorio", version="1.0.0", lifespan=lifespan)
app.include_router(predict.router)
app.include_router(model.router)


@app.get("/health", tags=["modele"])
def health() -> dict:
    return {"status": "up"}
