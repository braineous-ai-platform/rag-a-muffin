import json
from fastapi import FastAPI, HTTPException, requests
from app import schemas
from app import read_and_validate
from app import rest_api_client

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

    query = schemas.RawQueryIn(**payload.model_dump())
    print("##############QUERY############################")
    print(query)

    # vector_db_service = read_and_validate.VectorDBService()
    # emdeddings = vector_db_service.get_embeddings(**payload.model_dump())
    # print("##############GET_EMBEDDINGS####################")
    # print(emdeddings)

    ###########################################

    # Define the API endpoint URL
    api_url = "http://127.0.0.1:8080/hello"

    # Define the data payload to send in the POST request
    # This data is typically in JSON format for REST APIs
    prompt_payload = {
        "resp_1": "250",
        "resp_2": "293",
        "accuracy": 1.0
    }

    # Define headers, especially 'Content-Type' for JSON data
    headers = {
        "Content-Type": "application/json",
        "Goose": "love_you_maverick"
    }

    try:
        request_client = rest_api_client.rest_api_client()  # type: ignore
        print(request_client)

        response = request_client.post_request(
            api_url, prompt_payload, headers)

        print(response)

        # Check for a successful response (status code 200-299)
        # response.raise_for_status()

        # Parse the JSON response from the API
        # response_data = response.json()

        # print("POST request successful!")
        # print("Response data:")
        # print(json.dumps(response_data, indent=4))
        # print(f"Status code: {response.status_code}")

    except Exception as e:
        print(e)

        # print(f"An error occurred during the API call: {e}")
        # if hasattr(e, 'response') and e.response is not None:
        #    print(f"Error status code: {e.response.status_code}")
        #    print(f"Error response body: {e.response.text}")

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
