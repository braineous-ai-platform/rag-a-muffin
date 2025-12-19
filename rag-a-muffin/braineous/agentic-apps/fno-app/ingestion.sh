curl -s -v -X POST http://127.0.0.1:8080/fno/ingest \
  -H 'Content-Type: application/json' \
  -d '[
    {
      "id":"F102",
      "origin":"AUS",
      "dest":"DFW",
      "dep_utc":"2025-10-22T11:30:00Z",
      "arr_utc":"2025-10-22T12:40:00Z"
    },
    {
      "id":"F103",
      "origin":"DFW",
      "dest":"IAH",
      "dep_utc":"2025-10-22T13:30:00Z",
      "arr_utc":"2025-10-22T14:35:00Z"
    }
  ]'
