# seed_from_models.py
import weaviate  # type: ignore
from datetime import datetime, timezone
from typing import List
from pydantic import BaseModel

from models import Article


DOCS = [
    Article(title="PTO Policy Overview",
            content="Our standard paid time off policy covers vacation and personal days.",
            tags=["HR", "Policy"], published=datetime(2025, 1, 3, tzinfo=timezone.utc)),
    Article(title="Carryover Rules",
            content="Employees may carry over up to 5 PTO days into the next year if used by March 31.",
            tags=["HR", "Policy"], published=datetime(2025, 3, 10, tzinfo=timezone.utc)),
    Article(title="Holiday Blackout Dates",
            content="Year-end support requires a blackout period December 15–31 for most teams.",
            tags=["HR", "Ops"], published=datetime(2025, 6, 1, tzinfo=timezone.utc)),
    Article(title="Education Stipend Guide",
            content="Employees receive a $1,000 annual stipend for courses, books, or conference tickets.",
            tags=["HR", "Benefits"], published=datetime(2025, 2, 5, tzinfo=timezone.utc)),
    Article(title="Hybrid Search 101",
            content="Hybrid search blends BM25 keyword matching with vector similarity.",
            tags=["Tech", "Search"], published=datetime(2024, 12, 15, tzinfo=timezone.utc)),
    Article(title="Cosine vs Dot Product",
            content="Cosine distance is a strong default for text embeddings; normalize vectors for consistency.",
            tags=["Tech", "Embeddings"], published=datetime(2025, 4, 22, tzinfo=timezone.utc)),
]

with weaviate.connect_to_local() as c:
    col = c.collections.get("Article")
    for a in DOCS:
        col.data.insert(properties=a.model_dump())
print(f"Seeded {len(DOCS)} Article objects.")
