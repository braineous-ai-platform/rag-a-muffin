package ai.braineous.rag.prompt.cgo.querygen.services;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class QueryResultValidator implements PhaseResultValidator {

    private static final String DEFAULT_STAGE = "llm_response_validation";

    public QueryResultValidator() {
    }

    @Override
    public ValidationResult validate(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return error(
                    "queryresult.contract.empty",
                    "LLM response JSON is empty",
                    Collections.singletonMap("rawResponse", rawResponse)
            );
        }

        try {
            JsonElement rootElement = JsonParser.parseString(rawResponse);
            if (!rootElement.isJsonObject()) {
                return error(
                        "queryresult.contract.root_not_object",
                        "Expected JSON object at root",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            JsonObject root = rootElement.getAsJsonObject();

            if (!root.has("result") || !root.get("result").isJsonObject()) {
                return error(
                        "queryresult.contract.result_missing_or_invalid",
                        "Missing or invalid 'result' object",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            JsonObject result = root.getAsJsonObject("result");

            String ok = requireString(result, "ok");
            if (ok == null) {
                return error(
                        "queryresult.contract.result.ok_missing",
                        "Missing or invalid 'result.ok'",
                        Collections.singletonMap("result", result.toString())
                );
            }

            String code = requireString(result, "code");
            if (code == null) {
                return error(
                        "queryresult.contract.result.code_missing",
                        "Missing or invalid 'result.code'",
                        Collections.singletonMap("result", result.toString())
                );
            }

            String message = requireString(result, "message");
            if (message == null) {
                return error(
                        "queryresult.contract.result.message_missing",
                        "Missing or invalid 'result.message'",
                        Collections.singletonMap("result", result.toString())
                );
            }

            String stage = requireString(result, "stage");
            if (stage == null) {
                return error(
                        "queryresult.contract.result.stage_missing",
                        "Missing or invalid 'result.stage'",
                        Collections.singletonMap("result", result.toString())
                );
            }

            if (result.has("anchorId") && !result.get("anchorId").isJsonNull()) {
                if (requireString(result, "anchorId") == null) {
                    return error(
                            "queryresult.contract.result.anchorId_invalid",
                            "Invalid 'result.anchorId' (must be a non-empty string when present)",
                            Collections.singletonMap("result", result.toString())
                    );
                }
            }

            if (result.has("metadata") && !result.get("metadata").isJsonNull()) {
                if (!result.get("metadata").isJsonObject()) {
                    return error(
                            "queryresult.contract.result.metadata_invalid",
                            "Invalid 'result.metadata' (must be an object when present)",
                            Collections.singletonMap("result", result.toString())
                    );
                }
            }

            Map<String, Object> metadata = new HashMap<String, Object>();
            metadata.put("code", code);
            metadata.put("stage", stage);

            return ValidationResult.createInternal(
                    true,
                    "queryresult.contract.ok",
                    "Query result contract is valid",
                    DEFAULT_STAGE,
                    null,
                    metadata
            );
        } catch (JsonParseException e) {
            return error(
                    "queryresult.contract.invalid_json",
                    "Failed to parse LLM response as JSON: " + e.getMessage(),
                    Collections.singletonMap("rawResponse", rawResponse)
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
}