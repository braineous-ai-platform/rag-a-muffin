from fastapi import FastAPI, HTTPException
from app import schemas
from app import read_and_validate

# set_up the application (think round_circle processing API calls)
app = FastAPI(title="rag-a-muffin (User Query Service)")

####################################################

# bootstrap services (in_memory_db)
queries: list[schemas.RawQueryOut] = []
counter = 1

####################################################

# "raw_user_query"


@app.post("/query", response_model=schemas.RawQueryOut)
def query(payload: schemas.RawQueryIn):
    global counter
    obj = schemas.RawQueryOut(id=counter, **payload.model_dump())

    # counter += 1
    # queries.append(obj)

    vector_db_service = read_and_validate.VectorDBService()

    query = schemas.RawQueryIn(**payload.model_dump())
    print("##############QUERY############################")
    print(query)

    emdeddings = vector_db_service.get_embeddings(**payload.model_dump())
    print("##############GET_EMBEDDINGS####################")
    print(emdeddings)

    return obj

########################## future_endpoints_as_it_evolves##############################################

####################################################
# list


# @app.get("/orders", response_model=list[schemas.OrderOut])
# def list_orders():
#    return orders

####################################################

# read_by_id

####################################################

# update

####################################################
# delete

####################################################
