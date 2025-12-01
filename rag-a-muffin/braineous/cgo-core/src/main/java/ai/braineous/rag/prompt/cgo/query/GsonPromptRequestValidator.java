package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import com.google.gson.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default JSON-based PromptValidator using Gson.
 *
 * Validates the JSON produced by PromptBuilder:
 *
 * Expected high-level shape:
 *
 * {
 *   "meta": {
 *     "version": "v1",
 *     "query_kind": "validate_flight_airports",
 *     "description": "..."
 *   },
 *   "context": {
 *     "nodes": {
 *       "Flight:F100": {
 *         "id": "Flight:F100",
 *         "text": "...",
 *         "attributes": [ ... ],
 *         "mode": "relational"
 *       },
 *       ...
 *     }
 *   },
 *   "task": {
 *     "description": "...",
 *     "factId": "Flight:F100" // optional, only for ValidateTask
 *   },
 *   "response_contract": { ... },
 *   "instructions": [ "..." ],
 *   "llm_instructions": [ "..." ]
 * }
 *
 * On contract/shape failures:
 *  - returns ValidationResult with ok = false
 *  - code is a generic, domain-agnostic error code
 *  - message is safe for logs / observability
 */
public final class GsonPromptRequestValidator implements PhaseResultValidator {

    private static final String DEFAULT_STAGE = "prompt_contract_validation";

    private final Gson gson;

    public GsonPromptRequestValidator() {
        this(new Gson());
    }

    public GsonPromptRequestValidator(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    @Override
    public ValidationResult validate(String rawPrompt) {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            return error(
                    "prompt.contract.empty",
                    "Prompt JSON is empty",
                    Collections.singletonMap("prompt", rawPrompt)
            );
        }

        try {
            JsonElement rootElement = JsonParser.parseString(rawPrompt);
            if (!rootElement.isJsonObject()) {
                return error(
                        "prompt.contract.root_not_object",
                        "Expected JSON object at root",
                        Collections.singletonMap("rawPrompt", rawPrompt)
                );
            }

            JsonObject root = rootElement.getAsJsonObject();

            // ---- meta ----
            if (!root.has("meta") || !root.get("meta").isJsonObject()) {
                return error(
                        "prompt.contract.meta_missing_or_invalid",
                        "Missing or invalid 'meta' object",
                        Collections.singletonMap("rawPrompt", rawPrompt)
                );
            }
            JsonObject meta = root.getAsJsonObject("meta");

            String version = requireString(meta, "version");
            if (version == null) {
                return error(
                        "prompt.contract.meta.version_missing",
                        "Missing or invalid 'meta.version'",
                        Collections.singletonMap("meta", meta.toString())
                );
            }

            String queryKind = requireString(meta, "query_kind");
            if (queryKind == null) {
                return error(
                        "prompt.contract.meta.query_kind_missing",
                        "Missing or invalid 'meta.query_kind'",
                        Collections.singletonMap("meta", meta.toString())
                );
            }

            String description = requireString(meta, "description");
            if (description == null) {
                return error(
                        "prompt.contract.meta.description_missing",
                        "Missing or invalid 'meta.description'",
                        Collections.singletonMap("meta", meta.toString())
                );
            }

            // ---- context.nodes ----
            if (!root.has("context") || !root.get("context").isJsonObject()) {
                return error(
                        "prompt.contract.context_missing_or_invalid",
                        "Missing or invalid 'context' object",
                        Collections.singletonMap("rawPrompt", rawPrompt)
                );
            }
            JsonObject context = root.getAsJsonObject("context");

            if (!context.has("nodes") || !context.get("nodes").isJsonObject()) {
                return error(
                        "prompt.contract.context.nodes_missing_or_invalid",
                        "Missing or invalid 'context.nodes' object",
                        Collections.singletonMap("context", context.toString())
                );
            }

            // We only require nodes to be an object. We don't enforce node internals here
            // to keep this validator domain-agnostic and tolerant of future evolution.

            // ---- task ----
            if (!root.has("task") || !root.get("task").isJsonObject()) {
                return error(
                        "prompt.contract.task_missing_or_invalid",
                        "Missing or invalid 'task' object",
                        Collections.singletonMap("rawPrompt", rawPrompt)
                );
            }
            JsonObject task = root.getAsJsonObject("task");

            String taskDescription = requireString(task, "description");
            if (taskDescription == null) {
                return error(
                        "prompt.contract.task.description_missing",
                        "Missing or invalid 'task.description'",
                        Collections.singletonMap("task", task.toString())
                );
            }
            // 'factId' is optional and only meaningful for ValidateTask. If present, require string.
            if (task.has("factId") && requireString(task, "factId") == null) {
                return error(
                        "prompt.contract.task.factId_invalid",
                        "Invalid 'task.factId' (must be a non-empty string when present)",
                        Collections.singletonMap("task", task.toString())
                );
            }

            // ---- response_contract ----
            if (!root.has("response_contract") || !root.get("response_contract").isJsonObject()) {
                return error(
                        "prompt.contract.response_contract_missing_or_invalid",
                        "Missing or invalid 'response_contract' object",
                        Collections.singletonMap("rawPrompt", rawPrompt)
                );
            }

            // ---- instructions ----
            if (!root.has("instructions") || !root.get("instructions").isJsonArray()) {
                return error(
                        "prompt.contract.instructions_missing_or_invalid",
                        "Missing or invalid 'instructions' array",
                        Collections.singletonMap("rawPrompt", rawPrompt)
                );
            }
            JsonArray instructions = root.getAsJsonArray("instructions");
            if (!allStrings(instructions)) {
                return error(
                        "prompt.contract.instructions_not_all_strings",
                        "'instructions' must be an array of strings",
                        Collections.singletonMap("instructions", instructions.toString())
                );
            }

            // ---- llm_instructions (optional but recommended) ----
            if (root.has("llm_instructions")) {
                JsonElement llmInstructionsEl = root.get("llm_instructions");
                if (!llmInstructionsEl.isJsonArray() || !allStrings(llmInstructionsEl.getAsJsonArray())) {
                    return error(
                            "prompt.contract.llm_instructions_not_all_strings",
                            "'llm_instructions' must be an array of strings when present",
                            Collections.singletonMap("llm_instructions", llmInstructionsEl.toString())
                    );
                }
            }

            // If we got here, the prompt contract looks structurally valid
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("version", version);
            metadata.put("query_kind", queryKind);

            return ValidationResult.createInternal(
                    true,
                    "prompt.contract.ok",
                    "Prompt contract is valid",
                    DEFAULT_STAGE,
                    null,
                    metadata
            );
        } catch (JsonParseException e) {
            return error(
                    "prompt.contract.invalid_json",
                    "Failed to parse prompt as JSON: " + e.getMessage(),
                    Collections.singletonMap("rawPrompt", rawPrompt)
            );
        }
    }

    private ValidationResult error(String code, String message, Map<String, Object> metadata) {
        return ValidationResult.error(
                code,
                message,
                DEFAULT_STAGE,
                null,
                metadata != null ? metadata : Collections.emptyMap()
        );
    }

    private static String requireString(JsonObject obj, String field) {
        if (!obj.has(field)) {
            return null;
        }
        JsonElement el = obj.get(field);
        if (el == null || el.isJsonNull() || !el.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive prim = el.getAsJsonPrimitive();
        if (!prim.isString()) {
            return null;
        }
        String value = prim.getAsString();
        return value == null || value.isBlank() ? null : value;
    }

    private static boolean allStrings(JsonArray arr) {
        for (JsonElement el : arr) {
            if (!el.isJsonPrimitive()) {
                return false;
            }
            JsonPrimitive prim = el.getAsJsonPrimitive();
            if (!prim.isString()) {
                return false;
            }
        }
        return true;
    }
}

