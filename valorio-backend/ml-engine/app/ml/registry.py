"""Registry des artefacts ML : chargement/sauvegarde joblib + metadata.json, thread-safe."""
from __future__ import annotations

import json
import sys
import threading
from pathlib import Path

import joblib

ROOT = Path(__file__).resolve().parents[2]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

MODELS_DIR = ROOT / "models"
METADATA_PATH = MODELS_DIR / "metadata.json"
MODEL_NAMES = ("valorisation", "acteurs", "marche")

LOCK = threading.RLock()
_STATE: dict = {"valorisation": None, "acteurs": None, "marche": None, "metadata": None, "loaded": False}


def _artifact_paths() -> dict[str, Path]:
    return {name: MODELS_DIR / f"{name}.joblib" for name in MODEL_NAMES}


def artifacts_exist() -> bool:
    return all(p.exists() for p in _artifact_paths().values()) and METADATA_PATH.exists()


def _load_locked() -> None:
    for name, path in _artifact_paths().items():
        _STATE[name] = joblib.load(path)
    _STATE["metadata"] = json.loads(METADATA_PATH.read_text(encoding="utf-8"))
    _STATE["loaded"] = True


def ensure_models() -> dict:
    """Charge les artefacts ; lance un entraînement en-processus s'ils sont absents."""
    with LOCK:
        if _STATE["loaded"]:
            return _STATE["metadata"]
        if not artifacts_exist():
            from training.train import train_all

            train_all()
        _load_locked()
        return _STATE["metadata"]


def save_artifacts(valorisation: dict, acteurs: dict, marche: dict, metadata: dict) -> None:
    with LOCK:
        MODELS_DIR.mkdir(parents=True, exist_ok=True)
        joblib.dump(valorisation, MODELS_DIR / "valorisation.joblib")
        joblib.dump(acteurs, MODELS_DIR / "acteurs.joblib")
        joblib.dump(marche, MODELS_DIR / "marche.joblib")
        METADATA_PATH.write_text(
            json.dumps(metadata, ensure_ascii=False, indent=2), encoding="utf-8"
        )
        _load_locked()


def get_bundle() -> dict:
    ensure_models()
    with LOCK:
        return {
            "valorisation": _STATE["valorisation"],
            "acteurs": _STATE["acteurs"],
            "marche": _STATE["marche"],
            "metadata": _STATE["metadata"],
        }


def get_metadata() -> dict:
    ensure_models()
    with LOCK:
        return _STATE["metadata"]
