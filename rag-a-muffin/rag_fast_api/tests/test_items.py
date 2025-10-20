from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_crud_flow():
    r = client.post("/items", json={"name": "Alpha", "description": "first"})
    assert r.status_code == 200
    rid = r.json()["id"]

    assert client.get(f"/items/{rid}").status_code == 200
    r = client.put(f"/items/{rid}", json={"name": "Beta", "description": "updated"})
    assert r.status_code == 200 and r.json()["name"] == "Beta"

    r = client.get("/items")
    assert any(x["id"] == rid for x in r.json())

    assert client.delete(f"/items/{rid}").status_code == 204
