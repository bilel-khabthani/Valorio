def _payload(requete, candidats, topK=5):
    return {"requete": requete, "candidats": candidats, "topK": topK}


def test_articles_similaires_classes(client):
    requete = {"titre": "Ordinateur portable écran cassé", "description": "Portable 15 pouces, écran fendu",
               "categorie": "Electronique", "etat": "ENDOMMAGE", "prix": 300}
    candidats = [
        {"id": 1, "titre": "Ordinateur portable - ecran casse",
         "description": "Portable 15 pouces, ecran fende, fonctionne sous alimentation",
         "categorie": "Electronique", "etat": "ENDOMMAGE", "prix": 380},
        {"id": 2, "titre": "Table en bois - pied desserre",
         "description": "Table a manger en bois massif", "categorie": "Meubles",
         "etat": "BON", "prix": 60},
        {"id": 3, "titre": "Smartphone - batterie usee",
         "description": "Telephone tactile, batterie gonflee", "categorie": "Electronique",
         "etat": "ENDOMMAGE", "prix": 90},
    ]
    resp = client.post("/predict/marche", json=_payload(requete, candidats))
    assert resp.status_code == 200, resp.text
    data = resp.json()
    assert data["modeleVersion"] == "marche-tfidf-1.0.0"
    articles = data["articles"]
    assert len(articles) == 3
    scores = [a["scoreDeal"] for a in articles]
    assert scores == sorted(scores, reverse=True)
    assert articles[0]["articleId"] in (1, 3)
    for a in articles:
        assert 0.0 <= a["similarite"] <= 1.0
        assert 0.0 <= a["scoreDeal"] <= 1.0
        assert a["prixRecommande"] >= 1
        assert a["equitePrix"] in {"EXCELLENT_DEAL", "FAIR_MARKET", "OVERPRICED"}
        assert len(a["raisons"]) >= 2
    # électronique endommagé : 250 * 0.35 = 87.5 → prix 380 surpayé
    over = {a["articleId"]: a["equitePrix"] for a in articles}
    assert over[1] == "OVERPRICED"
    assert articles[0]["similarite"] >= articles[1]["similarite"] or articles[0]["articleId"] == 3


def test_prix_zero_est_excellent_deal(client):
    requete = {"titre": "Velo de ville", "categorie": "Velo & mobilite", "etat": "BON", "prix": 80}
    candidats = [{"id": 7, "titre": "Velo de ville - roue voilee", "categorie": "Velo & mobilite",
                  "etat": "BON", "prix": 0}]
    resp = client.post("/predict/marche", json=_payload(requete, candidats))
    assert resp.status_code == 200
    a = resp.json()["articles"][0]
    assert a["equitePrix"] == "EXCELLENT_DEAL"
    assert a["scoreDeal"] >= 0.6


def test_top_k_respected(client):
    requete = {"titre": "Chaise de bureau", "categorie": "Meubles", "etat": "BON", "prix": 30}
    candidats = [{"id": i, "titre": f"Chaise de bureau n°{i} roulettes cassees",
                  "categorie": "Meubles", "etat": "BON", "prix": 25 + i} for i in range(10)]
    resp = client.post("/predict/marche", json=_payload(requete, candidats, topK=3))
    assert resp.status_code == 200
    assert len(resp.json()["articles"]) == 3
