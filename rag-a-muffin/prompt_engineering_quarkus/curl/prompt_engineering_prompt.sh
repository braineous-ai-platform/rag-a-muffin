curl -s -v -X POST http://127.0.0.1:8080/hello \
  -H 'Content-Type: application/json' \
  -d '{"item":"Book","quantity":2,"price":10.5}'