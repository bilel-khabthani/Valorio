def _acteurs_payload(candidats):
    return {
        "typeActeur": "REPARATEUR",
        "contexte": {"categorie": "Electronique", "etat": "ENDOMMAGE",
                     "ageAnnees": 3, "valeurEstimee": 650, "ville": "Tunis"},
        "candidats": candidats,
        "position": {"latitude": 36.8065, "longitude": 10.1815},
    }


def test_classement_trie_et_coherent(client):
    candidats = [
        {"id": 1, "ville": "Tunis", "latitude": 36.82, "longitude": 10.17,
         "noteMoyenne": 4.8, "nbAvis": 120, "disponible": True,
         "competences": "reparation electronique, ecran", "categories": "Electronique"},
        {"id": 2, "ville": "Sfax", "latitude": 34.74, "longitude": 10.76,
         "noteMoyenne": 3.0, "nbAvis": 4, "disponible": False,
         "competences": "menuiserie", "categories": "Meubles"},
        {"id": 3, "ville": "Sousse", "latitude": 35.83, "longitude": 10.61,
         "noteMoyenne": 4.2, "nbAvis": 45, "disponible": True,
         "competences": "depannage telephone", "categories": "Electronique"},
    ]
    resp = client.post("/predict/acteurs", json=_acteurs_payload(candidats))
    assert resp.status_code == 200, resp.text
    data = resp.json()
    assert data["modeleVersion"] == "acteurs-gbr-1.0.0"
    classement = data["classement"]
    assert len(classement) == 3
    scores = [c["score"] for c in classement]
    assert scores == sorted(scores, reverse=True)
    by_id = {c["candidatId"]: c for c in classement}
    assert by_id[1]["score"] > by_id[2]["score"]
    assert by_id[1]["score"] > by_id[3]["score"]
    for c in classement:
        assert 0.0 <= c["score"] <= 1.0
        assert 0.0 <= c["probabiliteAcceptation"] <= 1.0
        assert c["score"] == c["probabiliteAcceptation"]
        assert c["distanceKm"] >= 0
        assert c["delaiJoursEstime"] >= 1
        assert len(c["raisons"]) > 0
    assert by_id[2]["distanceKm"] > by_id[1]["distanceKm"]


def test_distance_haversine_sfax(client):
    candidats = [{"id": 9, "ville": "Sfax", "latitude": 34.7406, "longitude": 10.7603,
                  "noteMoyenne": 4.0, "nbAvis": 30, "disponible": True}]
    resp = client.post("/predict/acteurs", json=_acteurs_payload(candidats))
    assert resp.status_code == 200
    d = resp.json()["classement"][0]["distanceKm"]
    assert 200 < d < 280
