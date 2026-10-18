def test_health(client):
    resp = client.get("/health")
    assert resp.status_code == 200
    assert resp.json() == {"status": "up"}


def test_model_metadata(client):
    resp = client.get("/model/metadata")
    assert resp.status_code == 200
    data = resp.json()
    assert data["actif"] is True
    assert data["versionValorisation"] == "valorisation-gbm-1.0.0"
    assert data["versionActeurs"] == "acteurs-gbr-1.0.0"
    assert data["versionMarche"] == "marche-tfidf-1.0.0"
    assert "accuracy" in data["metriques"]
    assert "f1_macro" in data["metriques"]
    assert data["metriques"]["accuracy"] > 0.5
    assert isinstance(data["topFeatures"], list)
    assert len(data["topFeatures"]) > 0
