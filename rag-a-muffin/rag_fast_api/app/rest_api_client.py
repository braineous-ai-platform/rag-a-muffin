import requests  # type: ignore
import json


class rest_api_client:
    def post_request(self, api_url: str, payload, headers) -> requests.Response:  # type: ignore
        # Make the POST request
        # The 'json' argument automatically serializes the dictionary to JSON and sets the Content-Type header
        response = requests.post(api_url, json=payload, headers=headers)

        return response


###########################################

# Define the API endpoint URL
api_url = "https://www.google.com"

# Define the data payload to send in the POST request
# This data is typically in JSON format for REST APIs
payload = {
    "name": "John Doe",
    "email": "john.doe@example.com",
    "age": 30
}

# Define headers, especially 'Content-Type' for JSON data
headers = {
    "Content-Type": "application/json"
}

###################################################
# try:
#    request_client = rest_api_client()
#    response = request_client.post_request(api_url, payload, headers)

# Check for a successful response (status code 200-299)
#    response.raise_for_status()

# Parse the JSON response from the API
#    response_data = response.json()

#    print("POST request successful!")
#    print("Response data:")
#    print(json.dumps(response_data, indent=4))
#    print(f"Status code: {response.status_code}")

# except requests.exceptions.RequestException as e:
#    print(f"An error occurred during the API call: {e}")
#    if hasattr(e, 'response') and e.response is not None:
#        print(f"Error status code: {e.response.status_code}")
#        print(f"Error response body: {e.response.text}")
