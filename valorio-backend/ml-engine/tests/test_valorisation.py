ROUTES = {"REPARATION", "DON", "RECYCLAGE", "REVENTE"}


def _post(client, payload):
    resp = client.post("/predict/valorisation", json=payload)
    assert resp.status_code == 200, resp.text
    return resp.json()


def test_ordinateur_endommage_reparation_ou_recyclage(client):
    data = _post(
        client,
        {
            "categorie": "Electronique",
            "etat": "ENDOMMAGE",
            "ageAnnees": 3,
            "valeurEstimee": 650,
            "coutReparationEstime": 400,
            "materiaux": ["plastique", "verre"],
        },
    )
    assert data["routeRecommandee"] in {"REPARATION", "RECYCLAGE"}
    assert 0.0 <= data["probabilite"] <= 1.0
    assert 0.0 <= data["confidenceScore"] <= 1.0
    assert data["modeleVersion"] == "valorisation-gbm-1.0.0"
    assert len(data["alternatives"]) == 3
    probs = [data["probabilite"]] + [a["probabilite"] for a in data["alternatives"]]
    assert probs == sorted(probs, reverse=True)
    assert sum(a["probabilite"] for a in data["alternatives"]) <= 1.001
    assert len(data["raisons"]) == 5
    for raison in data["raisons"]:
        assert raison["libelle"]
        assert isinstance(raison["contribution"], float)
        assert -1.0 <= raison["contribution"] <= 1.0
    features = {r["feature"] for r in data["raisons"]}
    assert "coutRatio" in features


def test_neuf_recent_haute_valeur_revente(client):
    data = _post(
        client,
        {"categorie": "Electronique", "etat": "NEUF", "ageAnnees": 0.5,
         "valeurEstimee": 900, "coutReparationEstime": 0},
    )
    assert data["routeRecommandee"] == "REVENTE"
    assert data["probabilite"] > 0.5


def test_hors_service_non_recyclable_don(client):
    data = _post(
        client,
        {"categorie": "Vetements", "etat": "HORS_SERVICE", "ageAnnees": 8,
         "valeurEstimee": 15, "materiaux": ["tissu"]},
    )
    assert data["routeRecommandee"] == "DON"


def test_unknown_category_and_etat_fallback(client):
    data = _post(
        client,
        {"categorie": "Poterie artisanale", "etat": "MOYEN",
         "ageAnnees": 4, "valeurEstimee": 50, "coutReparationEstime": 20},
    )
    assert data["routeRecommandee"] in ROUTES
    assert 0.0 <= data["probabilite"] <= 1.0


def test_taux_recyclabilite_derived_from_materiaux(client):
    data = _post(
        client,
        {"categorie": "Meubles", "etat": "HORS_SERVICE", "ageAnnees": 12,
         "valeurEstimee": 20, "materiaux": ["acier", "plastique"]},
    )
    assert data["routeRecommandee"] == "RECYCLAGE"
