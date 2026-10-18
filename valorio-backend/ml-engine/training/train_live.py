"""Re-entraînement optionnel à partir du feedback PostgreSQL (reco_feedback).

Gentil : saute proprement si psycopg2 ou la base ne sont pas disponibles.
"""
from __future__ import annotations

import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

MIN_FEEDBACK_ROWS = 100


def collect_feedback(conn) -> dict:
    with conn.cursor() as cur:
        cur.execute("SELECT utile, COUNT(*) FROM reco_feedback GROUP BY utile")
        rows = cur.fetchall()
    stats = {"utile": 0, "inutile": 0}
    for utile, count in rows:
        stats["utile" if utile else "inutile"] = int(count)
    return stats


def main() -> int:
    try:
        import psycopg2
    except ImportError:
        print("[train_live] psycopg2 non installé — re-entraînement live ignoré.")
        return 0

    url = os.environ.get("RECO_DATABASE_URL") or os.environ.get("DATABASE_URL")
    if not url:
        print("[train_live] Aucune URL de base (RECO_DATABASE_URL) — ignoré.")
        return 0

    try:
        conn = psycopg2.connect(url)
    except Exception as exc:  # noqa: BLE001 — repli gracieux, jamais d'erreur bloquante
        print(f"[train_live] Base injoignable ({exc.__class__.__name__}) — ignoré.")
        return 0

    try:
        stats = collect_feedback(conn)
        total = stats["utile"] + stats["inutile"]
        print(f"[train_live] Feedback récupéré : {total} lignes "
              f"({stats['utile']} utiles / {stats['inutile']} inutiles).")
        if total < MIN_FEEDBACK_ROWS:
            print(f"[train_live] Seuil de {MIN_FEEDBACK_ROWS} lignes non atteint — "
                  "réentraînement seed conservé.")
            return 0
        from training.train import train_all

        metadata = train_all(write_csvs=True)
        print(f"[train_live] Modèles ré-entraînés — accuracy="
              f"{metadata['metriques']['accuracy']:.4f}")
        return 0
    finally:
        conn.close()


if __name__ == "__main__":
    raise SystemExit(main())
