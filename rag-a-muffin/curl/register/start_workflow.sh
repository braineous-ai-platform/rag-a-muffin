curl -v -X POST http://localhost:8080/api/workflow/hello_world_java_0 \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "hello_world_java",
    "version": 1,
    "input": {
      "greeting": "Hello",
      "name": "Sohil"
    }
  }'