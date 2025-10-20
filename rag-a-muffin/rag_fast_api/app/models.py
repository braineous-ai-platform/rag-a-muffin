# models.py
from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, Field


class Article(BaseModel):
    title: str
    content: str
    tags: List[str] = Field(default_factory=list)
    published: datetime
