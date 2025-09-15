#!/usr/bin/env zsh
# bootstrap.sh — one-stop FastAPI CRUD scaffold + venv setup (zsh)
# USAGE:   source bootstrap.sh
# NOTES:   - First run: creates venv, installs deps, scaffolds files.
#          - Later runs: just re-activates venv and leaves your files intact.

# ---- helper: ensure we were sourced (so activation persists) ----
if [[ -z "$ZSH_EVAL_CONTEXT" || "$ZSH_EVAL_CONTEXT" != *":file"* ]]; then
  echo "⚠️  Run this script with: source bootstrap.sh"
  return 1 2>/dev/null || exit 1
fi

# ---- venv create/activate ----
if [ ! -d "venv" ]; then
  echo "📦 Creating virtual environment..."
  python3 -m venv venv || { echo "❌ Failed to create venv"; return 1 }
fi

echo "✅ Activating virtual environment..."
source venv/bin/activate || { echo "❌ Failed to activate venv"; return 1 }

# ---- deps ----
echo "⬆️  Upgrading pip..."
python -m pip install --upgrade pip

echo "📚 Installing FastAPI CRUD stack..."
# server + framework
pip install fastapi "uvicorn[standard]" \
# db & migrations (alembic optional; using create_all for scaffold)
  sqlalchemy \
# validation / settings / env
  pydantic pydantic-settings python-dotenv \
# testing
  pytest pytest-asyncio httpx >/dev/null

# ---- minimal requirements (for sharing) ----
if [ ! -f requirements.txt ]; then
  cat > requirements.txt <<'REQS'
fastapi
uvicorn[standard]
sqlalchemy
pydantic
pydantic-settings
python-dotenv
pytest
pytest-asyncio
httpx
weaviate-client==4.16.9
REQS
  echo "📝 Wrote requirements.txt"
fi

# ---- .gitignore ----
if [ ! -f .gitignore ]; then
  cat > .gitignore <<'GIT'
venv/
__pycache__/
*.pyc
.env
app.db
.pytest_cache/
.mypy_cache/
GIT
  echo "📝 Wrote .gitignore"
fi

# ---- env files ----
if [ ! -f ".env.example" ]; then
  cat > .env.example <<'ENVX'
APP_ENV=dev
# SQLite file in repo root
DATABASE_URL=sqlite:///./app.db
ENVX
  echo "📝 Wrote .env.example"
fi

if [ ! -f ".env" ]; then
  cp .env.example .env
  echo "📝 Wrote .env (copied from .env.example)"
fi

# ---- app scaffold ----
mkdir -p app tests

# __init__.py
if [ ! -f app/__init__.py ]; then
  echo "" > app/__init__.py
fi

# db.py
if [ ! -f app/db.py ]; then
  cat > app/db.py <<'PY'
import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from dotenv import load_dotenv

load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./app.db")

engine = create_engine(
    DATABASE_URL,
    connect_args={"check_same_thread": False} if DATABASE_URL.startswith("sqlite:///") else {}
)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)
Base = declarative_base()
PY
  echo "🧱 Wrote app/db.py"
fi

# models.py
if [ ! -f app/models.py ]; then
  cat > app/models.py <<'PY'
from sqlalchemy import Column, Integer, String
from .db import Base

class Item(Base):
    __tablename__ = "items"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    description = Column(String, nullable=True)
PY
  echo "🧱 Wrote app/models.py"
fi

# schemas.py
if [ ! -f app/schemas.py ]; then
  cat > app/schemas.py <<'PY'
from pydantic import BaseModel

class ItemIn(BaseModel):
    name: str
    description: str | None = None

class ItemOut(ItemIn):
    id: int
    class Config:
        from_attributes = True  # Pydantic v2: map from ORM
PY
  echo "🧱 Wrote app/schemas.py"
fi

# main.py
if [ ! -f app/main.py ]; then
  cat > app/main.py <<'PY'
from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from .db import Base, engine, SessionLocal
from . import models, schemas

app = FastAPI(title="CRUD Demo")

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.on_event("startup")
def on_startup():
    # For demos only; prefer Alembic for real migrations
    Base.metadata.create_all(bind=engine)

@app.post("/items", response_model=schemas.ItemOut)
def create_item(payload: schemas.ItemIn, db: Session = Depends(get_db)):
    obj = models.Item(name=payload.name, description=payload.description)
    db.add(obj); db.commit(); db.refresh(obj)
    return obj

@app.get("/items/{item_id}", response_model=schemas.ItemOut)
def read_item(item_id: int, db: Session = Depends(get_db)):
    obj = db.get(models.Item, item_id)
    if not obj:
        raise HTTPException(404, "Not found")
    return obj

@app.get("/items", response_model=list[schemas.ItemOut])
def list_items(db: Session = Depends(get_db)):
    return db.query(models.Item).all()

@app.put("/items/{item_id}", response_model=schemas.ItemOut)
def update_item(item_id: int, payload: schemas.ItemIn, db: Session = Depends(get_db)):
    obj = db.get(models.Item, item_id)
    if not obj:
        raise HTTPException(404, "Not found")
    obj.name = payload.name
    obj.description = payload.description
    db.commit(); db.refresh(obj)
    return obj

@app.delete("/items/{item_id}", status_code=204)
def delete_item(item_id: int, db: Session = Depends(get_db)):
    obj = db.get(models.Item, item_id)
    if obj:
        db.delete(obj); db.commit()
    return
PY
  echo "🧱 Wrote app/main.py"
fi

# simple test
if [ ! -f tests/test_items.py ]; then
  cat > tests/test_items.py <<'PY'
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
PY
  echo "🧪 Wrote tests/test_items.py"
fi

# ---- handy run alias for this shell session ----
alias api='uvicorn app.main:app --reload --host 0.0.0.0 --port 8000'

echo ""
echo "✅ Setup complete."
echo "   • venv active: $(which python)"
echo "   • Run API:      api   (alias for: uvicorn app.main:app --reload --port 8000)"
echo "   • Docs:         http://127.0.0.1:8000/docs"
echo "   • Tests:        pytest -q"