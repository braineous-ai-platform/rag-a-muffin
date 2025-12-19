#!/usr/bin/env bash
set -euo pipefail

BASE="http://127.0.0.1:8080"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

FLIGHTS_JSON="${HERE}/flights.json"

echo "0) PRECHECK: flights.json"
test -f "$FLIGHTS_JSON" || { echo "Missing: $FLIGHTS_JSON" >&2; exit 1; }
echo "   using: $FLIGHTS_JSON"
echo

echo "1) INGEST (file payload)"
curl -s -i -X POST "$BASE/fno/ingest" \
  -H 'Content-Type: application/json' \
  --data-binary "@${FLIGHTS_JSON}"
echo
echo

echo "2) QUERY"
curl -s -i -X POST "$BASE/fno/query" \
  -H 'Content-Type: application/json' \
  -d '{
    "meta": {
      "version": "v1",
      "query_kind": "validate_task",
      "description": "smoke: validate ingested graph"
    },
    "task": {
      "description": "validate Flight:F102 exists post-ingest",
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
