package ai.braineous.rag.prompt.cgo.querygen.services;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class QueryGenValidator implements PhaseResultValidator {

    private static final String DEFAULT_STAGE = "querygen_contract_validation";

    public QueryGenValidator() {
    }

    @Override
    public ValidationResult validate(String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return error(
                    "querygen.contract.empty",
                    "QueryGen JSON is empty",
                    Collections.singletonMap("rawQuery", rawQuery)
            );
        }

        try {
            JsonElement rootElement = JsonParser.parseString(rawQuery);
            if (!rootElement.isJsonObject()) {
                return error(
                        "querygen.contract.root_not_object",
                        "Expected JSON object at root",
                        Collections.singletonMap("rawQuery", rawQuery)
                );
            }

            JsonObject root = rootElement.getAsJsonObject();

            // ---- meta ----
            if (!root.has("meta") || !root.get("meta").isJsonObject()) {
                return error(
                        "querygen.contract.meta_missing_or_invalid",
                        "Missing or invalid 'meta' object",
                        Collections.singletonMap("rawQuery", rawQuery)
                );
            }

            JsonObject meta = root.getAsJsonObject("meta");

            String version = requireString(meta, "version");
            if (version == null) {
                return error(
                        "querygen.contract.meta.version_missing",
                        "Missing or invalid 'meta.version'",
                        Collections.singletonMap("meta", meta.toString())
                );
            }

            String queryKind = requireString(meta, "queryKind");
            if (queryKind == null) {
                return error(
                        "querygen.contract.meta.queryKind_missing",
                        "Missing or invalid 'meta.queryKind'",
                        Collections.singletonMap("meta", meta.toString())
                );
            }

            String description = requireString(meta, "description");
            if (description == null) {
                return error(
                        "querygen.contract.meta.description_missing",
                        "Missing or invalid 'meta.description'",
                        Collections.singletonMap("meta", meta.toString())
                );
            }

            // ---- context ----
            if (!root.has("context") || !root.get("context").isJsonObject()) {
                return error(
                        "querygen.contract.context_missing_or_invalid",
                        "Missing or invalid 'context' object",
                        Collections.singletonMap("rawQuery", rawQuery)
                );
            }

            JsonObject context = root.getAsJsonObject("context");
            if (!context.has("nodes") || !context.get("nodes").isJsonObject()) {
                return error(
                        "querygen.contract.context.nodes_missing_or_invalid",
                        "Missing or invalid 'context.nodes' object",
                        Collections.singletonMap("context", context.toString())
                );
            }

            // ---- task ----
            if (!root.has("task") || !root.get("task").isJsonObject()) {
                return error(
                        "querygen.contract.task_missing_or_invalid",
                        "Missing or invalid 'task' object",
                        Collections.singletonMap("rawQuery", rawQuery)
                );
            }

            JsonObject task = root.getAsJsonObject("task");

            String taskDescription = requireString(task, "description");
            if (taskDescription == null) {
                return error(
                        "querygen.contract.task.description_missing",
                        "Missing or invalid 'task.description'",
                        Collections.singletonMap("task", task.toString())
                );
            }

            if (task.has("factId") && !task.get("factId").isJsonNull()) {
                if (requireString(task, "factId") == null) {
                    return error(
                            "querygen.contract.task.factId_invalid",
                            "Invalid 'task.factId' (must be a non-empty string when present)",
                            Collections.singletonMap("task", task.toString())
                    );
                }
            }

            if (task.has("requestedFields") && !task.get("requestedFields").isJsonNull()) {
                if (!task.get("requestedFields").isJsonArray()) {
                    return error(
                            "querygen.contract.task.requestedFields_invalid",
                            "Invalid 'task.requestedFields' (must be an array when present)",
                            Collections.singletonMap("task", task.toString())
                    );
                }

                JsonArray requestedFields = task.getAsJsonArray("requestedFields");
                if (!allStringsOrNulls(requestedFields)) {
                    return error(
                            "querygen.contract.task.requestedFields_not_all_strings",
                            "'task.requestedFields' must contain only strings or nulls",
                            Collections.singletonMap("requestedFields", requestedFields.toString())
                    );
                }
            }

            if (task.has("relatedFactIds") && !task.get("relatedFactIds").isJsonNull()) {
                if (!task.get("relatedFactIds").isJsonArray()) {
                    return error(
                            "querygen.contract.task.relatedFactIds_invalid",
                            "Invalid 'task.relatedFactIds' (must be an array when present)",
                            Collections.singletonMap("task", task.toString())
                    );
                }

                JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
                if (!allStringsOrNulls(relatedFactIds)) {
                    return error(
                            "querygen.contract.task.relatedFactIds_not_all_strings",
                            "'task.relatedFactIds' must contain only strings or nulls",
                            Collections.singletonMap("relatedFactIds", relatedFactIds.toString())
                    );
                }
            }

            // ---- response_contract ----
            if (!root.has("response_contract") || !root.get("response_contract").isJsonObject()) {
                return error(
                        "querygen.contract.response_contract_missing_or_invalid",
                        "Missing or invalid 'response_contract' object",
                        Collections.singletonMap("rawQuery", rawQuery)
                );
            }

            // ---- llm_instructions ----
            if (!root.has("llm_instructions") || !root.get("llm_instructions").isJsonObject()) {
                return error(
                        "querygen.contract.llm_instructions_missing_or_invalid",
                        "Missing or invalid 'llm_instructions' object",
                        Collections.singletonMap("rawQuery", rawQuery)
                );
            }

            JsonObject llmInstructions = root.getAsJsonObject("llm_instructions");
            if (!llmInstructions.has("instructions") || !llmInstructions.get("instructions").isJsonArray()) {
                return error(
                        "querygen.contract.llm_instructions.instructions_missing_or_invalid",
                        "Missing or invalid 'llm_instructions.instructions' array",
                        Collections.singletonMap("llm_instructions", llmInstructions.toString())
                );
            }

            JsonArray instructions = llmInstructions.getAsJsonArray("instructions");
            if (!allStrings(instructions)) {
                return error(
                        "querygen.contract.llm_instructions.instructions_not_all_strings",
                        "'llm_instructions.instructions' must be an array of strings",
                        Collections.singletonMap("instructions", instructions.toString())
                );
            }

            // ---- llm_trace (optional) ----
            if (root.has("llm_trace") && !root.get("llm_trace").isJsonNull()) {
                if (!root.get("llm_trace").isJsonObject()) {
                    return error(
                            "querygen.contract.llm_trace_invalid",
                            "Invalid 'llm_trace' (must be an object when present)",
                            Collections.singletonMap("llm_trace", root.get("llm_trace").toString())
                    );
                }
            }

            // ---- llm_trace_instructions (optional) ----
            if (root.has("llm_trace_instructions") && !root.get("llm_trace_instructions").isJsonNull()) {
                JsonElement llmTraceInstructionsEl = root.get("llm_trace_instructions");

                if (!llmTraceInstructionsEl.isJsonObject()) {
                    return error(
                            "querygen.contract.llm_trace_instructions_invalid",
                            "Invalid 'llm_trace_instructions' (must be an object when present)",
                            Collections.singletonMap("llm_trace_instructions", llmTraceInstructionsEl.toString())
                    );
                }

                JsonObject llmTraceInstructions = llmTraceInstructionsEl.getAsJsonObject();
                if (llmTraceInstructions.has("instructions") && !llmTraceInstructions.get("instructions").isJsonNull()) {
                    if (!llmTraceInstructions.get("instructions").isJsonArray()) {
                        return error(
                                "querygen.contract.llm_trace_instructions.instructions_invalid",
                                "Invalid 'llm_trace_instructions.instructions' (must be an array when present)",
                                Collections.singletonMap("llm_trace_instructions", llmTraceInstructions.toString())
                        );
                    }

                    JsonArray traceInstructions = llmTraceInstructions.getAsJsonArray("instructions");
                    if (!allStrings(traceInstructions)) {
                        return error(
                                "querygen.contract.llm_trace_instructions.instructions_not_all_strings",
                                "'llm_trace_instructions.instructions' must be an array of strings",
                                Collections.singletonMap("instructions", traceInstructions.toString())
                        );
                    }
                }
            }

            Map<String, Object> metadata = new HashMap<String, Object>();
            metadata.put("version", version);
            metadata.put("queryKind", queryKind);

            return ValidationResult.createInternal(
                    true,
                    "querygen.contract.ok",
                    "QueryGen contract is valid",
                    DEFAULT_STAGE,
                    null,
                    metadata
            );
        } catch (JsonParseException e) {
            return error(
                    "querygen.contract.invalid_json",
                    "Failed to parse query as JSON: " + e.getMessage(),
                    Collections.singletonMap("rawQuery", rawQuery)
            );
        }
    }

    private ValidationResult error(String code, String message, Map<String, Object> metadata) {
        return ValidationResult.error(
                code,
                message,
                DEFAULT_STAGE,
                null,
                metadata != null ? metadata : Collections.<String, Object>emptyMap()
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

        if (!el.getAsJsonPrimitive().isString()) {
            return null;
        }

        String value = el.getAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value;
    }

    private static boolean allStrings(JsonArray arr) {
        for (JsonElement el : arr) {
            if (!el.isJsonPrimitive()) {
                return false;
            }
            if (!el.getAsJsonPrimitive().isString()) {
                return false;
            }
        }
        return true;
    }

    private static boolean allStringsOrNulls(JsonArray arr) {
        for (JsonElement el : arr) {
            if (el == null || el.isJsonNull()) {
                continue;
            }
            if (!el.isJsonPrimitive()) {
                return false;
            }
            if (!el.getAsJsonPrimitive().isString()) {
                return false;
            }
        }
        return true;
    }
}
