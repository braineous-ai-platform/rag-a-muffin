# http://localhost:8080/swagger-ui/index.html?configUrl=/api-docs/swagger-config#/workflow-resource/first_sample_workflow

curl -v -X 'POST' \
  'http://localhost:8080/api/workflow/first_sample_workflow' \
  -H 'accept: text/plain' \
  -H 'Content-Type: application/json' \
  -d '{}'