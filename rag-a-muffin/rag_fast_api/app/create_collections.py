
# create_collections.py (single file)
import weaviate  # type: ignore
from weaviate.classes.config import Property, DataType, Configure  # type: ignore
from typing import get_origin, get_args, Optional, List
from datetime import datetime, timezone
from pydantic import BaseModel

from models import Article

# --- mapper start ---
# def _scalar_dtype(py_t):
#    if py_t is str:
#        return DataType.TEXT
#    if py_t is int:
#        return DataType.INT
#    if py_t is float:
#        return DataType.NUMBER
#    if py_t is bool:
#        return DataType.BOOL
#    if py_t is datetime:
#        return DataType.DATE
#    return DataType.TEXT


# _ARRAY_MAP = {
#    DataType.TEXT:   DataType.TEXT_ARRAY,
#    DataType.INT:    DataType.INT_ARRAY,
#    DataType.NUMBER: DataType.NUMBER_ARRAY,
#    DataType.BOOL:   DataType.BOOL_ARRAY,
# }


# def to_weaviate_property(name: str, anno) -> Property:
#    origin = get_origin(anno)
#    args = get_args(anno)
#    if origin is Optional:
#        anno = args[0]
#        origin = get_origin(anno)
#        args = get_args(anno)
#    if origin in (list, List):
#        base = _scalar_dtype(args[0])
#        return Property(name=name, data_type=_ARRAY_MAP.get(base, DataType.TEXT_ARRAY))
#    return Property(name=name, data_type=_scalar_dtype(anno))
# --- mapper end ---


# class Article(BaseModel):
#    title: str
#    content: str
#    tags: List[str]
#    published: datetime


def main():
    # try:
    #    annotations = Article.model_annotations  # type: ignore[attr-defined]
    # except AttributeError:
    #    annotations = Article.__annotations__
    # props = [to_weaviate_property(k, t) for k, t in annotations.items()]
    with weaviate.connect_to_local() as c:
        # if c.collections.exists("Article"):
        #    c.collections.delete("Article")

        # c.collectons.append(Article(title="PTO Policy Overview",
        #              content="Our standard paid time off policy covers vacation and personal days.",
        #              tags=["HR", "Policy"], published=datetime(2025, 1, 3, tzinfo=timezone.utc)))

        # c.collections.create(
        #    name="Article",
        #    properties=article.model_dump(),
        #    vector_config=Configure.Vectors.text2vec_transformers(),
        # )

        article = Article(
            title="PTO Policy Overview",
            content="Our standard paid time off policy covers vacation and personal days.",
            tags=["HR", "Policy"],
            published=datetime(2025, 1, 3, tzinfo=timezone.utc),
        )
        # <-- grab the collection object
        col = c.collections.get("Article")
        col.data.insert(properties=article.model_dump(mode="json"))

        print("Created 'Article'.")


if __name__ == "__main__":
    main()
