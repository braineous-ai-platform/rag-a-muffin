#!/usr/bin/env bash
set -euo pipefail

BASE="http://127.0.0.1:8080"

echo "1) INGEST"
curl -s -i -X POST "$BASE/fno/ingest" \
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
echo
echo

echo "2) QUERY (NEGATIVE: empty task.description, query_kind present)"
curl -s -i -X POST http://127.0.0.1:8080/fno/query \
  -H 'Content-Type: application/json' \
  -d '{
    "meta": {
      "version": "v1",
      "query_kind": "validate_task",
      "description": "smoke: validate ingested graph"
    },
    "task": {
      "description": "",
      "factId": "Flight:F102"
    },
    "context": {
      "nodes": {}
    }
  }'
echo
echo


echo "3) OBSERVE"
curl -s -i "$BASE/fno/observe?queryKind=validate_task"
echo
