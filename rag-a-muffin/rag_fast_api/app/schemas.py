from pydantic import BaseModel

# Input (client → API)


class RawQueryIn(BaseModel):
    query: str

# Output (API → client)


class RawQueryOut(RawQueryIn):
    id: int
