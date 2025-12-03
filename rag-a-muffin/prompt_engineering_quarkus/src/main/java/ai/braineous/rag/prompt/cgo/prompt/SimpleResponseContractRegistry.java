package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Simple v1 registry implementation.
 *
 * For now we only support "validate_flight_airports" and hardcode
 * its response_contract and llm_instructions. This can be evolved
 * into a more generic/operator-driven registry later.
 */
public class SimpleResponseContractRegistry implements ResponseContractRegistry {

    private static final String QUERY_VALIDATE_FLIGHT_AIRPORTS = "validate_flight_airports";

    @Override
    public JsonObject responseContractFor(String queryKind) {
        if (QUERY_VALIDATE_FLIGHT_AIRPORTS.equals(queryKind)) {
            JsonObject root = new JsonObject();
            root.addProperty("type", "validation_result");
            root.addProperty("description", "Standard response for fact-level validation over a graph context.");

            // Minimal schema stub for now; we can expand this later
            JsonObject schema = new JsonObject();
            JsonObject result = new JsonObject();
            result.addProperty("type", "object");
            schema.add("result", result);
            root.add("schema", schema);

            return root;
        }

        // default empty contract for unknown query kinds (for now)
        JsonObject root = new JsonObject();
        root.addProperty("type", "unknown");
        root.add("schema", new JsonObject());
        return root;
    }

    @Override
    public List<String> llmInstructionsFor(String queryKind) {
        if (QUERY_VALIDATE_FLIGHT_AIRPORTS.equals(queryKind)) {
            return List.of(
                    "You are given a JSON object with 'meta', 'context', 'task', 'response_contract', and 'llm_instructions' fields.",
                    "Use only the 'context' and 'task' to understand what needs to be computed.",
                    "Locate the node in 'context.nodes' whose 'id' matches 'task.factId'.",
                    "Treat that node's 'text' field as JSON-encoded data and parse fields like 'id', 'kind', 'from', 'to'.",
                    "Use the 'response_contract' as the single source of truth for the shape of your reply.",
                    "Your answer must be a single JSON object that matches the 'result' schema in response_contract.",
                    "Do not include any keys that are not defined in the response_contract schema.",
                    "Do not include any natural language outside of JSON."
            );
        }

        // default: no extra instructions
        return List.of();
    }
}
