from pydantic import BaseModel

from typing import get_origin, get_args
from datetime import datetime
from uuid import UUID
from typing import List, Optional
from weaviate.classes.config import Property, DataType  # type: ignore

# Input (client → API)


class RawQueryIn(BaseModel):
    query: str

# Output (API → client)


class RawQueryOut(RawQueryIn):
    id: int

########################### weaviate_integration#############################################
# schema_from_pydantic.py


def to_weaviate_property(name: str, anno) -> Property:
    origin = get_origin(anno)
    args = get_args(anno)

    def scalar_dtype(py_t):
        if py_t is str:
            return DataType.TEXT
        if py_t is int:
            return DataType.INT
        if py_t is float:
            return DataType.NUMBER
        if py_t is bool:
            return DataType.BOOL  # BOOLEAN in older docs; BOOL in v4 client
        if py_t is datetime:
            return DataType.DATE
        if py_t is UUID:
            return DataType.UUID
        # Fallback:
        return DataType.TEXT

    # Handle Optional[T]
    if origin is Optional:
        anno = args[0]
        origin = get_origin(anno)
        args = get_args(anno)

    # Handle List[T]
    if origin in (list, List):
        base = scalar_dtype(args[0])
        array_map = {
            DataType.TEXT: DataType.TEXT_ARRAY,
            DataType.INT: DataType.INT_ARRAY,
            DataType.NUMBER: DataType.NUMBER_ARRAY,
            DataType.BOOL: DataType.BOOL_ARRAY,
            DataType.UUID: DataType.UUID_ARRAY,
        }
        return Property(name=name, data_type=array_map.get(base, DataType.TEXT_ARRAY))

    # Scalar
    return Property(name=name, data_type=scalar_dtype(anno))
