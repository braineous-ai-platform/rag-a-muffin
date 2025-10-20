# Fact Graph with Rules as Edges

A foundational idea for the Braineous agentic stack.

## Overview

- **Facts**: grounded, versioned, scored assertions.
- **Rules**: typed operators connecting facts.
- **Embeddings**: used for candidate discovery, not truth.

## Why it Matters

- Enables provenance and audit trails.
- Reduces token cost through reusable context.
- Detects contradictions and temporal drift.
- Supports causal reasoning over retrieved data.

## Minimal Schema

| Entity          | Description                                            |
| --------------- | ------------------------------------------------------ |
| `fact`          | subject–predicate–object + belief score                |
| `rule`          | implication, constraint, mapping, or temporal operator |
| `edge`          | typed connection with score and provenance             |
| `embedding_ref` | auxiliary link for similarity or discovery             |

## Example (Conceptual)

## See Also

- `/services/reasoning_engine/`
- `/docs/concepts/query_relationship_object.md`
